package com.francekim.empreintedelame.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.francekim.empreintedelame.data.model.ArchangelCardCastEntity
import com.francekim.empreintedelame.data.model.MirrorSoulAnswerEntity
import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.data.model.YiKingCastEntity

@Database(entities = [UserProfile::class, YiKingCastEntity::class, ArchangelCardCastEntity::class, MirrorSoulAnswerEntity::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun yiKingCastDao(): YiKingCastDao
    abstract fun archangelCardCastDao(): ArchangelCardCastDao
    abstract fun mirrorSoulAnswerDao(): MirrorSoulAnswerDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Ajout du décalage UTC de naissance, nécessaire au module Human Design
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN birthUtcOffsetMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Passage à 3 profils : la colonne id (toujours 1) devient slot (1, 2 ou 3).
        // Le profil existant (id=1) est conservé tel quel en slot=1.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE user_profile_new (" +
                        "slot INTEGER NOT NULL PRIMARY KEY, " +
                        "firstName TEXT NOT NULL, " +
                        "lastName TEXT NOT NULL, " +
                        "birthDay INTEGER NOT NULL, " +
                        "birthMonth INTEGER NOT NULL, " +
                        "birthYear INTEGER NOT NULL, " +
                        "birthHour INTEGER NOT NULL, " +
                        "birthMinute INTEGER NOT NULL, " +
                        "birthCity TEXT NOT NULL, " +
                        "birthUtcOffsetMinutes INTEGER NOT NULL DEFAULT 0)"
                )
                db.execSQL(
                    "INSERT INTO user_profile_new (slot, firstName, lastName, birthDay, birthMonth, birthYear, birthHour, birthMinute, birthCity, birthUtcOffsetMinutes) " +
                        "SELECT id, firstName, lastName, birthDay, birthMonth, birthYear, birthHour, birthMinute, birthCity, birthUtcOffsetMinutes FROM user_profile"
                )
                db.execSQL("DROP TABLE user_profile")
                db.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
            }
        }

        // Nouvelle table pour l'historique des tirages Yi-King, liée au profil actif.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE yi_king_cast (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "profileSlot INTEGER NOT NULL, " +
                        "timestampMillis INTEGER NOT NULL, " +
                        "theme TEXT NOT NULL, " +
                        "lineValues TEXT NOT NULL, " +
                        "primaryNumero INTEGER NOT NULL, " +
                        "secondaryNumero INTEGER)"
                )
            }
        }

        // Prise de note sur les tirages Yi-King + nouvelle table pour l'historique des tirages
        // de carte Archange (jusqu'ici éphémères, jamais sauvegardés).
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE yi_king_cast ADD COLUMN note TEXT")
                db.execSQL(
                    "CREATE TABLE archangel_card_cast (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "profileSlot INTEGER NOT NULL, " +
                        "timestampMillis INTEGER NOT NULL, " +
                        "cardId INTEGER NOT NULL, " +
                        "theme TEXT NOT NULL, " +
                        "note TEXT)"
                )
            }
        }

        // Nouvelle table pour l'historique des réponses du module Miroir de l'Âme
        // (uniquement les questions répondues — les phrases d'énergie ne sont pas sauvegardées).
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE mirror_soul_answer (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "profileSlot INTEGER NOT NULL, " +
                        "timestampMillis INTEGER NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "lifePathKey TEXT NOT NULL, " +
                        "texte TEXT NOT NULL, " +
                        "reponse TEXT NOT NULL)"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "empreinte_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build().also { INSTANCE = it }
            }
    }
}
