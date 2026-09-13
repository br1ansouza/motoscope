package dev.br1ansouza.motoscope.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sessions_new` (" +
                "`id` TEXT NOT NULL, " +
                "`startedAtEpochMillis` INTEGER NOT NULL, " +
                "`endedAtEpochMillis` INTEGER, " +
                "`status` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT INTO `sessions_new` " +
                "(`id`, `startedAtEpochMillis`, `endedAtEpochMillis`, `status`) " +
                "SELECT `id`, `startedAtEpochMillis`, `endedAtEpochMillis`, " +
                "CASE WHEN `endedAtEpochMillis` IS NULL THEN 'INTERRUPTED' ELSE 'FINISHED' END " +
                "FROM `sessions`"
        )
        db.execSQL("DROP TABLE `sessions`")
        db.execSQL("ALTER TABLE `sessions_new` RENAME TO `sessions`")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `telemetry_samples` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`sessionId` TEXT NOT NULL, " +
                "`metric` TEXT NOT NULL, " +
                "`value` REAL NOT NULL, " +
                "`unit` TEXT NOT NULL, " +
                "`source` TEXT NOT NULL, " +
                "`monotonicMillis` INTEGER NOT NULL, " +
                "`recordedAtEpochMillis` INTEGER NOT NULL, " +
                "FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS " +
                "`index_telemetry_samples_sessionId_metric_monotonicMillis` " +
                "ON `telemetry_samples` (`sessionId`, `metric`, `monotonicMillis`)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `session_events` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`sessionId` TEXT NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`detail` TEXT, " +
                "`monotonicMillis` INTEGER NOT NULL, " +
                "`recordedAtEpochMillis` INTEGER NOT NULL, " +
                "FOREIGN KEY(`sessionId`) REFERENCES `sessions`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_session_events_sessionId_monotonicMillis` " +
                "ON `session_events` (`sessionId`, `monotonicMillis`)"
        )
    }
}
