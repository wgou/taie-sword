use sea_orm::{
    ActiveModelTrait,
    EntityTrait,
    Set,
    ColumnTrait,
    QueryFilter,
    DbErr,
    PaginatorTrait,
    QueryOrder,
};
use log::{ info, error };
use crate::entities::user::{ self, CreateUserDto, UpdateUserDto, UserResponse };
use crate::database::get_db;

pub struct UserService;

impl UserService {
    /// 创建新用户
    pub async fn create_user(dto: CreateUserDto) -> Result<UserResponse, DbErr> {
        info!("👤 创建用户: {}", dto.username);

        let db = get_db()?;

        // 检查用户名是否已存在
        let existing = user::Entity
            ::find()
            .filter(user::Column::Username.eq(&dto.username))
            .one(db).await?;

        if existing.is_some() {
            error!("❌ 用户名已存在: {}", dto.username);
            return Err(DbErr::Custom(format!("用户名 '{}' 已存在", dto.username)));
        }

        let now = chrono::Utc::now().into();
        let user_model = user::ActiveModel {
            id: sea_orm::NotSet,
            username: Set(dto.username.clone()),
            password: Set(dto.password),
            age: Set(dto.age),
            created_at: Set(now),
            updated_at: Set(now),
        };

        let result = user_model.insert(db).await?;
        info!("✅ 用户创建成功: {} (ID: {})", dto.username, result.id);

        Ok(UserResponse::from(result))
    }

    /// 根据 ID 获取用户
    pub async fn get_user_by_id(id: i32) -> Result<Option<UserResponse>, DbErr> {
        info!("🔍 查找用户 ID: {}", id);

        let db = get_db()?;
        let user = user::Entity::find_by_id(id).one(db).await?;

        match user {
            Some(u) => {
                info!("✅ 找到用户: {}", u.username);
                Ok(Some(UserResponse::from(u)))
            }
            None => {
                info!("❌ 未找到用户 ID: {}", id);
                Ok(None)
            }
        }
    }

    /// 根据用户名获取用户
    pub async fn get_user_by_username(username: &str) -> Result<Option<UserResponse>, DbErr> {
        info!("🔍 查找用户名: {}", username);

        let db = get_db()?;
        let user = user::Entity::find().filter(user::Column::Username.eq(username)).one(db).await?;

        match user {
            Some(u) => {
                info!("✅ 找到用户: {}", u.username);
                Ok(Some(UserResponse::from(u)))
            }
            None => {
                info!("❌ 未找到用户名: {}", username);
                Ok(None)
            }
        }
    }

    /// 获取所有用户（分页）
    pub async fn get_all_users(
        page: u64,
        per_page: u64
    ) -> Result<(Vec<UserResponse>, u64), DbErr> {
        info!("📃 获取用户列表，页码: {}, 每页: {}", page, per_page);

        let db = get_db()?;

        // 获取总数
        let total = user::Entity::find().count(db).await?;

        // 获取用户列表
        let users = user::Entity
            ::find()
            .order_by_asc(user::Column::Id)
            .paginate(db, per_page)
            .fetch_page(page).await?;

        let user_responses: Vec<UserResponse> = users.into_iter().map(UserResponse::from).collect();

        info!("✅ 获取到 {} 个用户，总数: {}", user_responses.len(), total);
        Ok((user_responses, total))
    }

    /// 更新用户
    pub async fn update_user(id: i32, dto: UpdateUserDto) -> Result<Option<UserResponse>, DbErr> {
        info!("✏️ 更新用户 ID: {}", id);

        let db = get_db()?;

        // 先查找用户
        let user = user::Entity::find_by_id(id).one(db).await?;
        let Some(user) = user else {
            info!("❌ 用户不存在 ID: {}", id);
            return Ok(None);
        };

        // 如果要更新用户名，检查是否冲突
        if let Some(ref new_username) = dto.username {
            if new_username != &user.username {
                let existing = user::Entity
                    ::find()
                    .filter(user::Column::Username.eq(new_username))
                    .filter(user::Column::Id.ne(id))
                    .one(db).await?;

                if existing.is_some() {
                    error!("❌ 用户名已存在: {}", new_username);
                    return Err(DbErr::Custom(format!("用户名 '{}' 已存在", new_username)));
                }
            }
        }

        let mut user_active: user::ActiveModel = user.into();

        // 更新字段
        if let Some(username) = dto.username {
            user_active.username = Set(username);
        }
        if let Some(password) = dto.password {
            user_active.password = Set(password);
        }
        if let Some(age) = dto.age {
            user_active.age = Set(age);
        }

        let updated_user = user_active.update(db).await?;
        info!("✅ 用户更新成功: {}", updated_user.username);

        Ok(Some(UserResponse::from(updated_user)))
    }

    /// 删除用户
    pub async fn delete_user(id: i32) -> Result<bool, DbErr> {
        info!("🗑️ 删除用户 ID: {}", id);

        let db = get_db()?;

        let result = user::Entity::delete_by_id(id).exec(db).await?;

        if result.rows_affected > 0 {
            info!("✅ 用户删除成功 ID: {}", id);
            Ok(true)
        } else {
            info!("❌ 用户不存在 ID: {}", id);
            Ok(false)
        }
    }

    /// 验证用户登录
    pub async fn verify_user(
        username: &str,
        password: &str
    ) -> Result<Option<UserResponse>, DbErr> {
        info!("🔐 验证用户登录: {}", username);

        let db = get_db()?;
        let user = user::Entity
            ::find()
            .filter(user::Column::Username.eq(username))
            .filter(user::Column::Password.eq(password))
            .one(db).await?;

        match user {
            Some(u) => {
                info!("✅ 用户验证成功: {}", u.username);
                Ok(Some(UserResponse::from(u)))
            }
            None => {
                info!("❌ 用户验证失败: {}", username);
                Ok(None)
            }
        }
    }

    /// 获取用户总数
    pub async fn get_user_count() -> Result<u64, DbErr> {
        let db = get_db()?;
        let count = user::Entity::find().count(db).await?;
        info!("📊 用户总数: {}", count);
        Ok(count)
    }
}
