# Data Model: Add and Track Data

**Date**: 2026-03-12 | **Feature Branch**: `001-add-track-data`

## Entities

### DataType

A user-defined category for daily tracking.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | `Long` | Primary key, auto-generate | Room auto-incremented ID |
| `emoji` | `String` | Not null, not blank | Single emoji character (validated at UI layer) |
| `description` | `String` | Not null, max 60 chars | User-visible label |
| `createdAt` | `Long` | Not null | Epoch millis (`System.currentTimeMillis()`) for sort order |

**Room Entity**:
```kotlin
@Entity(
    tableName = "data_types",
    indices = [Index(value = ["emoji", "description"], unique = true)]
)
data class DataTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

**Sort order**: `ORDER BY createdAt DESC` (newest first, per spec).

**Uniqueness constraint**: Composite unique index on `(emoji, description)` prevents duplicate data types (FR-016).

---

### DailyEntry

A record linking a calendar date to an active data type.

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | `Long` | Primary key, auto-generate | Room auto-incremented ID |
| `date` | `String` | Not null | ISO-8601 date string (`"2026-03-12"`) |
| `dataTypeId` | `Long` | Foreign key → `data_types.id` | CASCADE on delete |

**Room Entity**:
```kotlin
@Entity(
    tableName = "daily_entries",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date", "dataTypeId"], unique = true),
        Index(value = ["dataTypeId"])
    ]
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dataTypeId: Long
)
```

**Design decision — presence-based vs boolean toggle**:
An entry row *existing* means the data type is active for that day. No row = inactive. This avoids storing explicit `false` values and simplifies queries ("which data types are active on date X?" = `SELECT * FROM daily_entries WHERE date = ?`).

**Uniqueness constraint**: Composite unique index on `(date, dataTypeId)` prevents toggling the same data type twice for one day.

**Cascade delete**: When a data type is deleted, all its daily entries are automatically removed (FR-004).

---

### OnboardingSuggestion (in-memory only)

Pre-defined data-type templates shown during first-run onboarding. **Not persisted** in the database — defined as a static Kotlin list.

| Field | Type | Notes |
|-------|------|-------|
| `emoji` | `String` | Suggestion emoji |
| `description` | `String` | Suggestion label |
| `selectedByDefault` | `Boolean` | Whether pre-checked in onboarding UI |

```kotlin
data class OnboardingSuggestion(
    val emoji: String,
    val description: String,
    val selectedByDefault: Boolean = false
)

val DEFAULT_SUGGESTIONS = listOf(
    OnboardingSuggestion("🩸", "Period / bleeding", selectedByDefault = true),
    OnboardingSuggestion("😊", "Mood"),
    OnboardingSuggestion("💊", "Medication taken"),
    OnboardingSuggestion("😴", "Sleep quality"),
    OnboardingSuggestion("🏃", "Exercise"),
    OnboardingSuggestion("🚪", "Left the house"),
    OnboardingSuggestion("👥", "Saw friends"),
    OnboardingSuggestion("🤕", "Headache"),
    OnboardingSuggestion("⚡", "Energy level")
)
```

## Relationships

```
DataType (1) ──────< DailyEntry (many)
   │                      │
   └── id (PK)            ├── dataTypeId (FK → data_types.id, CASCADE)
                          └── date (ISO-8601 string)
```

- **One-to-many**: Each `DataType` can have zero or more `DailyEntry` records across different dates.
- **Cascade delete**: Deleting a `DataType` removes all associated `DailyEntry` rows.
- **No direct relationship** between dates — a day is simply a `String` key. No separate "Day" entity is needed.

## DAO Interfaces

### DataTypeDao

```kotlin
@Dao
interface DataTypeDao {
    @Query("SELECT * FROM data_types ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<DataTypeEntity>>

    @Query("SELECT COUNT(*) FROM data_types")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(dataType: DataTypeEntity): Long

    @Update
    suspend fun update(dataType: DataTypeEntity)

    @Delete
    suspend fun delete(dataType: DataTypeEntity)

    @Query("SELECT * FROM data_types WHERE emoji = :emoji AND description = :description LIMIT 1")
    suspend fun findDuplicate(emoji: String, description: String): DataTypeEntity?
}
```

### DailyEntryDao

```kotlin
@Dao
interface DailyEntryDao {
    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun getEntriesForDateFlow(date: String): Flow<List<DailyEntryEntity>>

    @Query("SELECT DISTINCT date FROM daily_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getDatesWithEntriesFlow(startDate: String, endDate: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DailyEntryEntity>)

    @Query("DELETE FROM daily_entries WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    @Transaction
    suspend fun replaceEntriesForDate(date: String, entries: List<DailyEntryEntity>) {
        deleteAllForDate(date)
        insertAll(entries)
    }

    @Query("DELETE FROM daily_entries WHERE dataTypeId NOT IN (SELECT id FROM data_types)")
    suspend fun deleteOrphanedEntries()
}
```

## State Machines

### Day Panel Save Flow

```
User taps day → Load entries for date → Show toggles (on/off per data type)
  → User toggles data types → User taps "Save"
  → replaceEntriesForDate(date, activeDataTypeIds)
  → Bottom sheet dismisses → Calendar refreshes indicators
```

**Explicit save** (per spec clarification): Changes are only persisted when the user taps "Save" / "Done". Dismissing the bottom sheet without saving discards changes.

### Data Type Lifecycle

```
           ┌─────────┐
           │ Created  │ ← insert()
           └────┬─────┘
                │
           ┌────▼─────┐
           │  Active   │ ← normal state, visible in lists and day panel
           └────┬─────┘
                │
        ┌───────┴───────┐
        │               │
   ┌────▼─────┐   ┌────▼──────┐
   │  Edited  │   │  Deleted  │ ← delete() with CASCADE
   └────┬─────┘   └───────────┘
        │
   ┌────▼─────┐
   │  Active   │ ← back to normal with updated fields
   └──────────┘
```

No soft delete — deletion is permanent with cascade (per FR-004).

## Validation Rules

| Rule | Entity | Field | Enforcement |
|------|--------|-------|-------------|
| Emoji required | DataType | `emoji` | UI validation before save |
| Description required | DataType | `description` | UI validation before save |
| Description max 60 chars | DataType | `description` | UI character counter + validation |
| No duplicate (emoji + description) | DataType | composite | Unique DB index + pre-check query |
| Valid date format | DailyEntry | `date` | ISO-8601 string from `LocalDate.toString()` |
| Foreign key integrity | DailyEntry | `dataTypeId` | Room FK constraint |
