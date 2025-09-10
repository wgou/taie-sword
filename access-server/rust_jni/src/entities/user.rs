use sea_orm::entity::prelude::*;
use sea_orm::{ Set, ActiveModelTrait };
use serde::{ Deserialize, Serialize };

#[derive(Clone, Debug, PartialEq, DeriveEntityModel, Eq, Serialize, Deserialize)]
#[sea_orm(table_name = "users")]
pub struct Model {
    #[sea_orm(primary_key)]
    pub id: i32,
    #[sea_orm(column_type = "String(Some(255))", unique)]
    pub username: String,
    #[sea_orm(column_type = "String(Some(255))")]
    pub password: String,
    pub age: i32,
    #[sea_orm(column_type = "TimestampWithTimeZone")]
    pub created_at: DateTimeWithTimeZone,
    #[sea_orm(column_type = "TimestampWithTimeZone")]
    pub updated_at: DateTimeWithTimeZone,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveRelation)]
pub enum Relation {}

impl ActiveModelBehavior for ActiveModel {}

// DTO for creating a user
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CreateUserDto {
    pub username: String,
    pub password: String,
    pub age: i32,
}

// DTO for updating a user
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct UpdateUserDto {
    pub username: Option<String>,
    pub password: Option<String>,
    pub age: Option<i32>,
}

// Response DTO
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct UserResponse {
    pub id: i32,
    pub username: String,
    pub age: i32,
    pub created_at: String,
    pub updated_at: String,
}

impl From<Model> for UserResponse {
    fn from(user: Model) -> Self {
        Self {
            id: user.id,
            username: user.username,
            age: user.age,
            created_at: user.created_at.format("%Y-%m-%d %H:%M:%S").to_string(),
            updated_at: user.updated_at.format("%Y-%m-%d %H:%M:%S").to_string(),
        }
    }
}
