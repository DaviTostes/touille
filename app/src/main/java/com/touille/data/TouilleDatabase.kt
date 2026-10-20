package com.touille.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        ShoppingItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class TouilleDatabase : RoomDatabase() {

    abstract fun dao(): TouilleDao

    companion object {

        private val Migrate4To5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `shopping_items` (" +
                        "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "`ingredientKey` TEXT, " +
                        "`customName` TEXT NOT NULL, " +
                        "`amount` TEXT NOT NULL, " +
                        "`checked` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_shopping_items_ingredientKey` " +
                        "ON `shopping_items` (`ingredientKey`)"
                )
            }
        }

        private val Migrate5To6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // "Molho de soja" is how the bottle is labelled, "shoyu" is what everyone says.
                db.execSQL("UPDATE ingredients SET namePt = 'Molho shoyu' WHERE `key` = 'Soy sauce'")
            }
        }

        fun build(context: Context): TouilleDatabase {
            lateinit var db: TouilleDatabase
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val prefs = context.getSharedPreferences("touille", Context.MODE_PRIVATE)
            db = Room.databaseBuilder(context, TouilleDatabase::class.java, "touille.db")
                .addMigrations(Migrate4To5, Migrate5To6)
                .addCallback(object : Callback() {
                    override fun onOpen(connection: SupportSQLiteDatabase) {
                        super.onOpen(connection)
                        scope.launch {
                            // Only sync when the seed version changes. insertIngredients
                            // ignores conflicts, so ownership is never reset.
                            if (prefs.getInt(KEY_SEED_VERSION, 0) != IngredientSeed.VERSION) {
                                db.dao().insertIngredients(IngredientSeed.all)
                                prefs.edit()
                                    .putInt(KEY_SEED_VERSION, IngredientSeed.VERSION)
                                    .apply()
                            }
                        }
                    }
                })
                .build()
            return db
        }

        private const val KEY_SEED_VERSION = "seedVersion"
    }
}
