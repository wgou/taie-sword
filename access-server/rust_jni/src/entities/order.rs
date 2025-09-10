use sea_orm::entity::prelude::*;
use sea_orm::{ Set, ActiveModelTrait };
use serde::{ Deserialize, Serialize };

#[derive(Clone, Debug, PartialEq, DeriveEntityModel, Eq, Serialize, Deserialize)]
#[sea_orm(table_name = "orders")]
pub struct Model {
    #[sea_orm(primary_key)]
    pub id: i32,
    #[sea_orm(column_type = "Integer")]
    pub user_id: i32,
    #[sea_orm(column_type = "String(Some(255))")]
    pub product_name: String,
    #[sea_orm(column_type = "Integer")]
    pub quantity: i32,
    #[sea_orm(column_type = "Decimal(Some((10, 2)))")]
    pub price: Decimal,
    #[sea_orm(column_type = "Decimal(Some((10, 2)))")]
    pub total_amount: Decimal,
    #[sea_orm(column_type = "String(Some(50))")]
    pub status: String, // pending, confirmed, shipped, delivered, cancelled
    #[sea_orm(column_type = "Text", nullable)]
    pub notes: Option<String>,
    #[sea_orm(column_type = "TimestampWithTimeZone")]
    pub created_at: DateTimeWithTimeZone,
    #[sea_orm(column_type = "TimestampWithTimeZone")]
    pub updated_at: DateTimeWithTimeZone,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveRelation)]
pub enum Relation {
    #[sea_orm(
        belongs_to = "crate::entities::user::Entity",
        from = "Column::UserId",
        to = "crate::entities::user::Column::Id"
    )]
    User,
}

impl Related<crate::entities::user::Entity> for Entity {
    fn to() -> RelationDef {
        Relation::User.def()
    }
}

impl ActiveModelBehavior for ActiveModel {}

// 订单状态枚举
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
pub enum OrderStatus {
    Pending,
    Confirmed,
    Shipped,
    Delivered,
    Cancelled,
}

impl OrderStatus {
    pub fn as_str(&self) -> &'static str {
        match self {
            OrderStatus::Pending => "pending",
            OrderStatus::Confirmed => "confirmed",
            OrderStatus::Shipped => "shipped",
            OrderStatus::Delivered => "delivered",
            OrderStatus::Cancelled => "cancelled",
        }
    }

    pub fn from_str(s: &str) -> Option<Self> {
        match s.to_lowercase().as_str() {
            "pending" => Some(OrderStatus::Pending),
            "confirmed" => Some(OrderStatus::Confirmed),
            "shipped" => Some(OrderStatus::Shipped),
            "delivered" => Some(OrderStatus::Delivered),
            "cancelled" => Some(OrderStatus::Cancelled),
            _ => None,
        }
    }
}

// DTO for creating an order
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CreateOrderDto {
    pub user_id: i32,
    pub product_name: String,
    pub quantity: i32,
    pub price: f64,
    pub notes: Option<String>,
}

// DTO for updating an order
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct UpdateOrderDto {
    pub product_name: Option<String>,
    pub quantity: Option<i32>,
    pub price: Option<f64>,
    pub status: Option<String>,
    pub notes: Option<String>,
}

// Response DTO
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct OrderResponse {
    pub id: i32,
    pub user_id: i32,
    pub product_name: String,
    pub quantity: i32,
    pub price: String, // 转换为字符串以避免精度问题
    pub total_amount: String,
    pub status: String,
    pub notes: Option<String>,
    pub created_at: String,
    pub updated_at: String,
}

impl From<Model> for OrderResponse {
    fn from(order: Model) -> Self {
        Self {
            id: order.id,
            user_id: order.user_id,
            product_name: order.product_name,
            quantity: order.quantity,
            price: order.price.to_string(),
            total_amount: order.total_amount.to_string(),
            status: order.status,
            notes: order.notes,
            created_at: order.created_at.format("%Y-%m-%d %H:%M:%S").to_string(),
            updated_at: order.updated_at.format("%Y-%m-%d %H:%M:%S").to_string(),
        }
    }
}

// 订单统计 DTO
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct OrderStatsResponse {
    pub total_orders: u64,
    pub total_amount: String,
    pub pending_orders: u64,
    pub completed_orders: u64,
}
