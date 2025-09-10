use sea_orm::{
    ActiveModelTrait,
    EntityTrait,
    Set,
    ColumnTrait,
    QueryFilter,
    DbErr,
    PaginatorTrait,
    QueryOrder,
    QuerySelect,
};
use log::{ info, error };
use crate::entities::order::{
    self,
    CreateOrderDto,
    UpdateOrderDto,
    OrderResponse,
    OrderStatus,
    OrderStatsResponse,
};
use crate::entities::user;
use crate::database::get_db;
use rust_decimal::Decimal;
use std::str::FromStr;

pub struct OrderService;

impl OrderService {
    /// 创建新订单
    pub async fn create_order(dto: CreateOrderDto) -> Result<OrderResponse, DbErr> {
        info!("📦 创建订单: {} x{}", dto.product_name, dto.quantity);

        let db = get_db()?;

        // 验证用户是否存在
        let user_exists = user::Entity::find_by_id(dto.user_id).one(db).await?;
        if user_exists.is_none() {
            error!("❌ 用户不存在: {}", dto.user_id);
            return Err(DbErr::Custom(format!("用户ID {} 不存在", dto.user_id)));
        }

        // 计算总金额
        let price_decimal = Decimal::from_str(&dto.price.to_string()).map_err(|e|
            DbErr::Custom(format!("价格格式错误: {}", e))
        )?;
        let quantity_decimal = Decimal::from_str(&dto.quantity.to_string()).map_err(|e|
            DbErr::Custom(format!("数量格式错误: {}", e))
        )?;
        let total_amount = price_decimal * quantity_decimal;

        let now = chrono::Utc::now().into();
        let order_model = order::ActiveModel {
            id: sea_orm::NotSet,
            user_id: Set(dto.user_id),
            product_name: Set(dto.product_name.clone()),
            quantity: Set(dto.quantity),
            price: Set(price_decimal),
            total_amount: Set(total_amount),
            status: Set(OrderStatus::Pending.as_str().to_string()),
            notes: Set(dto.notes),
            created_at: Set(now),
            updated_at: Set(now),
        };

        let result = order_model.insert(db).await?;
        info!("✅ 订单创建成功: {} (ID: {})", dto.product_name, result.id);

        Ok(OrderResponse::from(result))
    }

    /// 根据 ID 获取订单
    pub async fn get_order_by_id(id: i32) -> Result<Option<OrderResponse>, DbErr> {
        info!("🔍 查找订单 ID: {}", id);

        let db = get_db()?;
        let order = order::Entity::find_by_id(id).one(db).await?;

        match order {
            Some(o) => {
                info!("✅ 找到订单: {}", o.product_name);
                Ok(Some(OrderResponse::from(o)))
            }
            None => {
                info!("❌ 未找到订单 ID: {}", id);
                Ok(None)
            }
        }
    }

    /// 根据用户ID获取订单列表
    pub async fn get_orders_by_user_id(
        user_id: i32,
        page: u64,
        per_page: u64
    ) -> Result<(Vec<OrderResponse>, u64), DbErr> {
        info!("📃 获取用户 {} 的订单列表，页码: {}, 每页: {}", user_id, page, per_page);

        let db = get_db()?;

        // 获取总数
        let total = order::Entity
            ::find()
            .filter(order::Column::UserId.eq(user_id))
            .count(db).await?;

        // 获取订单列表
        let orders = order::Entity
            ::find()
            .filter(order::Column::UserId.eq(user_id))
            .order_by_desc(order::Column::CreatedAt)
            .paginate(db, per_page)
            .fetch_page(page).await?;

        let order_responses: Vec<OrderResponse> = orders
            .into_iter()
            .map(OrderResponse::from)
            .collect();

        info!("✅ 获取到 {} 个订单，总数: {}", order_responses.len(), total);
        Ok((order_responses, total))
    }

    /// 获取所有订单（分页）
    pub async fn get_all_orders(
        page: u64,
        per_page: u64
    ) -> Result<(Vec<OrderResponse>, u64), DbErr> {
        info!("📃 获取订单列表，页码: {}, 每页: {}", page, per_page);

        let db = get_db()?;

        // 获取总数
        let total = order::Entity::find().count(db).await?;

        // 获取订单列表
        let orders = order::Entity
            ::find()
            .order_by_desc(order::Column::CreatedAt)
            .paginate(db, per_page)
            .fetch_page(page).await?;

        let order_responses: Vec<OrderResponse> = orders
            .into_iter()
            .map(OrderResponse::from)
            .collect();

        info!("✅ 获取到 {} 个订单，总数: {}", order_responses.len(), total);
        Ok((order_responses, total))
    }

    /// 更新订单
    pub async fn update_order(
        id: i32,
        dto: UpdateOrderDto
    ) -> Result<Option<OrderResponse>, DbErr> {
        info!("✏️ 更新订单 ID: {}", id);

        let db = get_db()?;

        // 先查找订单
        let order = order::Entity::find_by_id(id).one(db).await?;
        let Some(order) = order else {
            info!("❌ 订单不存在 ID: {}", id);
            return Ok(None);
        };

        let mut order_active: order::ActiveModel = order.into();

        // 更新字段
        if let Some(product_name) = dto.product_name {
            order_active.product_name = Set(product_name);
        }
        if let Some(quantity) = dto.quantity {
            order_active.quantity = Set(quantity);
            // 如果数量变化，重新计算总金额
            if let Some(price) = dto.price {
                let price_decimal = Decimal::from_str(&price.to_string()).map_err(|e|
                    DbErr::Custom(format!("价格格式错误: {}", e))
                )?;
                let quantity_decimal = Decimal::from_str(&quantity.to_string()).map_err(|e|
                    DbErr::Custom(format!("数量格式错误: {}", e))
                )?;
                order_active.price = Set(price_decimal);
                order_active.total_amount = Set(price_decimal * quantity_decimal);
            }
        } else if let Some(price) = dto.price {
            let price_decimal = Decimal::from_str(&price.to_string()).map_err(|e|
                DbErr::Custom(format!("价格格式错误: {}", e))
            )?;
            order_active.price = Set(price_decimal);
            // 重新计算总金额
            if let sea_orm::ActiveValue::Set(quantity) = &order_active.quantity {
                let quantity_decimal = Decimal::from_str(&quantity.to_string()).map_err(|e|
                    DbErr::Custom(format!("数量格式错误: {}", e))
                )?;
                order_active.total_amount = Set(price_decimal * quantity_decimal);
            }
        }
        if let Some(status) = dto.status {
            // 验证状态有效性
            if OrderStatus::from_str(&status).is_some() {
                order_active.status = Set(status);
            } else {
                return Err(DbErr::Custom(format!("无效的订单状态: {}", status)));
            }
        }
        if let Some(notes) = dto.notes {
            order_active.notes = Set(Some(notes));
        }

        order_active.updated_at = Set(chrono::Utc::now().into());

        let updated_order = order_active.update(db).await?;
        info!("✅ 订单更新成功: {}", updated_order.product_name);

        Ok(Some(OrderResponse::from(updated_order)))
    }

    /// 删除订单
    pub async fn delete_order(id: i32) -> Result<bool, DbErr> {
        info!("🗑️ 删除订单 ID: {}", id);

        let db = get_db()?;

        let result = order::Entity::delete_by_id(id).exec(db).await?;

        if result.rows_affected > 0 {
            info!("✅ 订单删除成功 ID: {}", id);
            Ok(true)
        } else {
            info!("❌ 订单不存在 ID: {}", id);
            Ok(false)
        }
    }

    /// 根据状态获取订单
    pub async fn get_orders_by_status(
        status: &str,
        page: u64,
        per_page: u64
    ) -> Result<(Vec<OrderResponse>, u64), DbErr> {
        info!("📃 获取状态为 {} 的订单列表", status);

        // 验证状态有效性
        if OrderStatus::from_str(status).is_none() {
            return Err(DbErr::Custom(format!("无效的订单状态: {}", status)));
        }

        let db = get_db()?;

        // 获取总数
        let total = order::Entity::find().filter(order::Column::Status.eq(status)).count(db).await?;

        // 获取订单列表
        let orders = order::Entity
            ::find()
            .filter(order::Column::Status.eq(status))
            .order_by_desc(order::Column::CreatedAt)
            .paginate(db, per_page)
            .fetch_page(page).await?;

        let order_responses: Vec<OrderResponse> = orders
            .into_iter()
            .map(OrderResponse::from)
            .collect();

        info!("✅ 获取到 {} 个订单，总数: {}", order_responses.len(), total);
        Ok((order_responses, total))
    }

    /// 更新订单状态
    pub async fn update_order_status(
        id: i32,
        status: &str
    ) -> Result<Option<OrderResponse>, DbErr> {
        info!("📝 更新订单 {} 状态为: {}", id, status);

        // 验证状态有效性
        if OrderStatus::from_str(status).is_none() {
            return Err(DbErr::Custom(format!("无效的订单状态: {}", status)));
        }

        let db = get_db()?;

        // 先查找订单
        let order = order::Entity::find_by_id(id).one(db).await?;
        let Some(order) = order else {
            info!("❌ 订单不存在 ID: {}", id);
            return Ok(None);
        };

        let mut order_active: order::ActiveModel = order.into();
        order_active.status = Set(status.to_string());
        order_active.updated_at = Set(chrono::Utc::now().into());

        let updated_order = order_active.update(db).await?;
        info!("✅ 订单状态更新成功: {} -> {}", updated_order.product_name, status);

        Ok(Some(OrderResponse::from(updated_order)))
    }

    /// 获取订单统计信息
    pub async fn get_order_stats() -> Result<OrderStatsResponse, DbErr> {
        info!("📊 获取订单统计信息");

        let db = get_db()?;

        // 获取总订单数
        let total_orders = order::Entity::find().count(db).await?;

        // 获取总金额
        let orders = order::Entity::find().all(db).await?;
        let total_amount: Decimal = orders
            .iter()
            .map(|o| &o.total_amount)
            .sum();

        // 获取待处理订单数
        let pending_orders = order::Entity
            ::find()
            .filter(order::Column::Status.eq(OrderStatus::Pending.as_str()))
            .count(db).await?;

        // 获取已完成订单数
        let completed_orders = order::Entity
            ::find()
            .filter(order::Column::Status.eq(OrderStatus::Delivered.as_str()))
            .count(db).await?;

        let stats = OrderStatsResponse {
            total_orders,
            total_amount: total_amount.to_string(),
            pending_orders,
            completed_orders,
        };

        info!("✅ 订单统计完成: 总订单数 {}, 总金额 {}", total_orders, total_amount);
        Ok(stats)
    }

    /// 获取订单总数
    pub async fn get_order_count() -> Result<u64, DbErr> {
        let db = get_db()?;
        let count = order::Entity::find().count(db).await?;
        info!("📊 订单总数: {}", count);
        Ok(count)
    }
}
