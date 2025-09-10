use std::sync::OnceLock;
use sea_orm::{ ConnectOptions, ConnectionTrait, Database, DatabaseConnection, DbErr, Statement };
use log::{ info, error };
use std::path::Path;

// 全局数据库连接
static DB_CONNECTION: OnceLock<DatabaseConnection> = OnceLock::new();

/// 初始化数据库连接
pub async fn init_database(db_path: &str) -> Result<(), DbErr> {
    info!("🗄️ 初始化数据库: {}", db_path);

    // 确保数据库文件的目录存在
    if let Some(parent) = Path::new(db_path).parent() {
        if let Err(e) = std::fs::create_dir_all(parent) {
            error!("❌ 创建数据库目录失败: {}", e);
            return Err(DbErr::Custom(format!("创建目录失败: {}", e)));
        }
    }

    let database_url = format!("sqlite://{}?mode=rwc", db_path);
    info!("🔗 数据库连接字符串: {}", database_url);

    let mut opt = ConnectOptions::new(database_url);
    opt.max_connections(100)
        .min_connections(5)
        .connect_timeout(std::time::Duration::from_secs(8))
        .acquire_timeout(std::time::Duration::from_secs(8))
        .idle_timeout(std::time::Duration::from_secs(8))
        .max_lifetime(std::time::Duration::from_secs(8))
        .sqlx_logging(true)
        .sqlx_logging_level(log::LevelFilter::Info);

    let db = Database::connect(opt).await?;

    // 设置全局连接
    if DB_CONNECTION.set(db).is_err() {
        error!("❌ 数据库连接已经初始化");
        return Err(DbErr::Custom("数据库连接已经初始化".to_string()));
    }

    // 创建表
    create_tables().await?;

    info!("✅ 数据库初始化完成");
    Ok(())
}

/// 获取数据库连接
pub fn get_db() -> Result<&'static DatabaseConnection, DbErr> {
    DB_CONNECTION.get().ok_or_else(|| {
        error!("❌ 数据库未初始化");
        DbErr::Custom("数据库未初始化".to_string())
    })
}

/// 创建数据库表
async fn create_tables() -> Result<(), DbErr> {
    use sea_orm::{ Schema, Statement };
    use sea_query::SqliteQueryBuilder;
    use crate::entities::{ user, order };

    let db = get_db()?;
    let schema = Schema::new(sea_orm::DatabaseBackend::Sqlite);

    // 创建 users 表
    let stmt = schema.create_table_from_entity(user::Entity);
    let sql = stmt.to_string(SqliteQueryBuilder);
    info!("🔨 创建表 SQL: {}", sql);

    let create_table_stmt = Statement::from_string(sea_orm::DatabaseBackend::Sqlite, sql);

    match db.execute(create_table_stmt).await {
        Ok(_) => {
            info!("✅ users 表创建成功");
        }
        Err(e) => {
            // 表可能已存在，这是正常的
            info!("ℹ️ users 表创建结果: {}", e);
        }
    }

    // 创建 orders 表
    let stmt = schema.create_table_from_entity(order::Entity);
    let sql = stmt.to_string(SqliteQueryBuilder);
    info!("🔨 创建订单表 SQL: {}", sql);

    let create_table_stmt = Statement::from_string(sea_orm::DatabaseBackend::Sqlite, sql);

    match db.execute(create_table_stmt).await {
        Ok(_) => {
            info!("✅ orders 表创建成功");
        }
        Err(e) => {
            // 表可能已存在，这是正常的
            info!("ℹ️ orders 表创建结果: {}", e);
        }
    }

    Ok(())
}

/// 检查数据库连接
pub async fn check_connection() -> Result<bool, DbErr> {
    let db = get_db()?;
    match db.ping().await {
        Ok(_) => {
            info!("✅ 数据库连接正常");
            Ok(true)
        }
        Err(e) => {
            error!("❌ 数据库连接失败: {}", e);
            Err(e)
        }
    }
}

/// 执行数据库迁移
pub async fn migrate_database() -> Result<(), DbErr> {
    let db = get_db()?;

    // 检查 class 字段是否已存在
    let check_column_sql =
        r#"
        SELECT COUNT(*) as count 
        FROM pragma_table_info('users') 
        WHERE name = 'class'
    "#;

    let result = db.query_one(
        Statement::from_string(sea_orm::DatabaseBackend::Sqlite, check_column_sql.to_string())
    ).await?;

    if let Some(row) = result {
        let count: i32 = row.try_get("", "count")?;
        if count == 0 {
            // 字段不存在，执行添加
            info!("🔄 添加 class 字段到 users 表");
            let add_column_sql = "ALTER TABLE users ADD COLUMN class TEXT";

            let stmt = Statement::from_string(
                sea_orm::DatabaseBackend::Sqlite,
                add_column_sql.to_string()
            );

            match db.execute(stmt).await {
                Ok(_) => {
                    info!("✅ class 字段添加成功");
                }
                Err(e) => {
                    error!("❌ 添加 class 字段失败: {}", e);
                    return Err(e);
                }
            }
        } else {
            info!("ℹ️ class 字段已存在，跳过迁移");
        }
    }

    Ok(())
}
