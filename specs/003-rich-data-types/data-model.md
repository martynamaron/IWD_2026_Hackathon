# Data Model: Rich Data Input Types

**Date**: 2026-03-12 | **Feature Branch**: `003-rich-data-types`  
**Database version**: 1 → 2 (Room auto-migration)

---

## Enumerations

### InputType

Discriminates the input style for a data type. Stored as a `String` in the database using the enum's `name`.

```kotlin
enum class InputType { TOGGLE, SCALE, MULTIPLE_CHOICE }
```

| Value | Description |
|---|---|
| `TOGGLE` | Binary on/off. Existing behaviour. |
| `SCALE` | Integer 0–5. Recorded value stored in `daily_entries.scaleValue`. |
| `MULTIPLE_CHOICE` | Set of labelled emoji options. Selections stored in `multi_choice_selections`. |

---

## Entities

### DataTypeEntity *(updated — DB v2)*

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generate | Unchanged |
| `emoji` | `String` | Not null, not blank | Unchanged |
| `description` | `String` | Not null, max 60 chars | Unchanged |
| `createdAt` | `Long` | Not null | Epoch millis — unchanged |
| `inputType` | `String` | Not null, default `'TOGGLE'` | **NEW** — stores `InputType.name` |

**Migration note**: Room auto-migration adds the `inputType` column with `DEFAULT 'TOGGLE'`. All pre-existing data types automatically become `TOGGLE`.

**Lock invariant**: Once `inputType ≠ 'TOGGLE'`, the value is immutable. The UI enforces this; no DB constraint needed for the prototype.

```kotlin
@Entity(
    tableName = "data_types",
    indices = [Index(value = ["emoji", "description"], unique = true)]
)
data class DataTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emoji: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val inputType: String = InputType.TOGGLE.name   // NEW
)
```

---

### MultipleChoiceOptionEntity *(new — DB v2)*

Defines the available options for a `MULTIPLE_CHOICE` data type. Not used by `TOGGLE` or `SCALE` types.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generate | |
| `dataTypeId` | `Long` | FK → `data_types.id` CASCADE DELETE | |
| `emoji` | `String` | Not null, not blank | Single emoji character |
| `label` | `String` | Not null, 1–30 chars | Human-readable option name |
| `sortOrder` | `Int` | Not null, default 0 | Display order (insertion order) |

**Validation rules** (enforced at UI layer, before DB write):
- Min 2 options per data type (FR-016).
- Max 10 options per data type (FR-003 + edge case).
- No duplicate `(emoji, label)` pair within the same data type (FR-015).
- Label must be non-empty (FR-017).

```kotlin
@Entity(
    tableName = "multiple_choice_options",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dataTypeId"])]
)
data class MultipleChoiceOptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dataTypeId: Long,
    val emoji: String,
    val label: String,
    val sortOrder: Int = 0
)
```

---

### DailyEntryEntity *(updated — DB v2)*

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generate | Unchanged |
| `date` | `String` | Not null | ISO-8601 date — unchanged |
| `dataTypeId` | `Long` | FK → `data_types.id` CASCADE | Unchanged |
| `scaleValue` | `Int?` | Nullable | **NEW** — holds `0..5` for Scale types; `null` for Toggle rows |

**How each input type uses this table:**

| Type | Row present? | `scaleValue` | Meaning |
|---|---|---|---|
| TOGGLE | Yes | `null` | Data type was active / on |
| TOGGLE | No | — | Data type was inactive / unrecorded |
| SCALE | Yes | `0..5` | Score was recorded |
| SCALE | No | — | Score is unrecorded (null state) |
| MULTIPLE_CHOICE | *Never* | — | Selections live in `multi_choice_selections` |

**The null ↔ 0 distinction for Scale**: A row with `scaleValue = 0` is a valid recorded score of zero. The absence of a row means the user made no selection — these are distinct states (FR-005, edge case in spec).

**Migration note**: Room auto-migration adds `scaleValue` as a nullable INTEGER column with no default (null). Existing Toggle entries get `scaleValue = null` automatically.

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
    val dataTypeId: Long,
    val scaleValue: Int? = null   // NEW
)
```

---

### MultiChoiceSelectionEntity *(new — DB v2)*

Records which options are selected for a (date × data type) pair. One row per selected option. Zero rows for a (date, dataTypeId) = unrecorded.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `Long` | PK, auto-generate | |
| `date` | `String` | Not null | ISO-8601 date |
| `dataTypeId` | `Long` | FK → `data_types.id` CASCADE DELETE | |
| `optionId` | `Long` | FK → `multiple_choice_options.id` CASCADE DELETE | |

**Uniqueness**: Composite unique index on `(date, dataTypeId, optionId)` prevents the same option being selected twice for one day.

**Cascade behaviour**: Deleting a data type removes its options, which cascade-deletes all associated selections. Deleting an option also removes its historical selections (edge case: "Editing options on an existing multiple choice type" → past entries for that option become unrecorded).

```kotlin
@Entity(
    tableName = "multi_choice_selections",
    foreignKeys = [
        ForeignKey(
            entity = DataTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["dataTypeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MultipleChoiceOptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["optionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date", "dataTypeId", "optionId"], unique = true),
        Index(value = ["dataTypeId"]),
        Index(value = ["optionId"])
    ]
)
data class MultiChoiceSelectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dataTypeId: Long,
    val optionId: Long
)
```

---

### OnboardingSuggestion *(updated — in-memory only)*

Gains `inputType` and optional `defaultOptions` fields so Mood and Exercise produce the correct entity types during DB seeding.

```kotlin
data class OptionSuggestion(val emoji: String, val label: String)

data class OnboardingSuggestion(
    val emoji: String,
    val description: String,
    val selectedByDefault: Boolean = false,
    val inputType: InputType = InputType.TOGGLE,   // NEW
    val defaultOptions: List<OptionSuggestion> = emptyList()   // NEW
)

val DEFAULT_SUGGESTIONS = listOf(
    OnboardingSuggestion("🩸", "Period / bleeding", selectedByDefault = true),
    OnboardingSuggestion(
        "😊", "Mood",
        inputType = InputType.SCALE
    ),
    OnboardingSuggestion("💊", "Medication taken"),
    OnboardingSuggestion("😴", "Sleep quality"),
    OnboardingSuggestion(
        "🏃", "Exercise",
        inputType = InputType.MULTIPLE_CHOICE,
        defaultOptions = listOf(
            OptionSuggestion("🏃", "Running"),
            OptionSuggestion("🏊", "Swimming"),
            OptionSuggestion("🎾", "Tennis"),
            OptionSuggestion("💃", "Dancing")
        )
    ),
    OnboardingSuggestion("🚪", "Left the house"),
    OnboardingSuggestion("👥", "Saw friends"),
    OnboardingSuggestion("🤕", "Headache"),
    OnboardingSuggestion("⚡", "Energy level")
)
```

---

## Relationships

```
DataType (1) ──────< MultipleChoiceOption (many)
DataType (1) ──────< DailyEntry (many)            [Toggle + Scale only]
DataType (1) ──────< MultiChoiceSelection (many)  [MultipleChoice only]
MultipleChoiceOption (1) ──< MultiChoiceSelection (many)

DataType.id (PK)
    │
    ├── DailyEntry.dataTypeId (FK, CASCADE)
    │       └── date + scaleValue
    │
    ├── MultipleChoiceOption.dataTypeId (FK, CASCADE)
    │       └── emoji + label + sortOrder
    │       └──── MultiChoiceSelection.optionId (FK, CASCADE)
    │                   └── date + dataTypeId + optionId
    │
    └── MultiChoiceSelection.dataTypeId (FK, CASCADE)
```

---

## DAO Interfaces

### MultipleChoiceOptionDao *(new)*

```kotlin
@Dao
interface MultipleChoiceOptionDao {
    @Query("SELECT * FROM multiple_choice_options WHERE dataTypeId = :dataTypeId ORDER BY sortOrder ASC")
    fun getOptionsForDataTypeFlow(dataTypeId: Long): Flow<List<MultipleChoiceOptionEntity>>

    @Query("SELECT * FROM multiple_choice_options WHERE dataTypeId = :dataTypeId ORDER BY sortOrder ASC")
    suspend fun getOptionsForDataType(dataTypeId: Long): List<MultipleChoiceOptionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(options: List<MultipleChoiceOptionEntity>)

    @Delete
    suspend fun delete(option: MultipleChoiceOptionEntity)

    @Query("DELETE FROM multiple_choice_options WHERE dataTypeId = :dataTypeId")
    suspend fun deleteAllForDataType(dataTypeId: Long)
}
```

### MultiChoiceSelectionDao *(new)*

```kotlin
@Dao
interface MultiChoiceSelectionDao {
    @Query("SELECT * FROM multi_choice_selections WHERE date = :date AND dataTypeId = :dataTypeId")
    fun getSelectionsFlow(date: String, dataTypeId: Long): Flow<List<MultiChoiceSelectionEntity>>

    @Query("SELECT DISTINCT date FROM multi_choice_selections WHERE date BETWEEN :startDate AND :endDate")
    fun getDatesWithSelectionsFlow(startDate: String, endDate: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM multi_choice_selections WHERE dataTypeId = :dataTypeId")
    suspend fun countSelectionsForDataType(dataTypeId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(selections: List<MultiChoiceSelectionEntity>)

    @Query("DELETE FROM multi_choice_selections WHERE date = :date AND dataTypeId = :dataTypeId")
    suspend fun deleteAllForDateAndType(date: String, dataTypeId: Long)

    @Query("DELETE FROM multi_choice_selections WHERE dataTypeId = :dataTypeId")
    suspend fun deleteAllForDataType(dataTypeId: Long)
}
```

### Updated DataTypeDao (additions)

```kotlin
@Query("SELECT * FROM data_types WHERE id = :id LIMIT 1")
suspend fun getById(id: Long): DataTypeEntity?

@Query("SELECT * FROM data_types WHERE inputType != 'TOGGLE'")
suspend fun getNonToggleTypes(): List<DataTypeEntity>
```

### Updated DailyEntryDao (additions)

```kotlin
// For Scale: count affected entries before type migration
@Query("SELECT COUNT(*) FROM daily_entries WHERE dataTypeId = :dataTypeId")
suspend fun countEntriesForDataType(dataTypeId: Long): Int

// Replace all entries for a date (handles both Toggle and Scale)
// Already covered by deleteAllForDate + insertAll combination in existing DAO

// Get all dates with any recorded entry (UNION with multi_choice_selections)
@Query("""
    SELECT DISTINCT date FROM daily_entries WHERE date BETWEEN :startDate AND :endDate
    UNION
    SELECT DISTINCT date FROM multi_choice_selections WHERE date BETWEEN :startDate AND :endDate
""")
fun getDatesWithAnyEntryFlow(startDate: String, endDate: String): Flow<List<String>>
```

> **Note**: `getDatesWithAnyEntryFlow` replaces the existing `getDatesWithEntriesFlow` for calendar indicator queries.

---

## State Transitions

### Data Type Input Type Change

```
TOGGLE ──→ SCALE            (one-way, requires confirmation + entry migration)
TOGGLE ──→ MULTIPLE_CHOICE  (one-way, requires confirmation + option creation + entry migration)
SCALE  ──→ (locked)         (no further changes allowed)
MULTIPLE_CHOICE ──→ (locked)
```

### Daily Value States

| Input Type | Unrecorded | Recorded |
|---|---|---|
| TOGGLE | No row in `daily_entries` | Row in `daily_entries` (scaleValue=null) |
| SCALE | No row in `daily_entries` | Row in `daily_entries` (scaleValue=0..5) |
| MULTIPLE_CHOICE | No rows in `multi_choice_selections` for (date, dataTypeId) | ≥1 rows in `multi_choice_selections` |

---

## Room Database Configuration

```kotlin
@Database(
    entities = [
        DataTypeEntity::class,
        DailyEntryEntity::class,
        MultipleChoiceOptionEntity::class,    // NEW
        MultiChoiceSelectionEntity::class     // NEW
    ],
    version = 2,                              // BUMPED from 1
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)       // NEW
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataTypeDao(): DataTypeDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun multipleChoiceOptionDao(): MultipleChoiceOptionDao     // NEW
    abstract fun multiChoiceSelectionDao(): MultiChoiceSelectionDao     // NEW
}
```
