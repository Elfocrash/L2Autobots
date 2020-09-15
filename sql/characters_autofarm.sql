CREATE TABLE IF NOT EXISTS character_autofarm (
  `obj_Id` INT UNSIGNED NOT NULL DEFAULT 0,
  `classId` INT UNSIGNED NOT NULL DEFAULT 0,
  `combat_prefs` LONGTEXT NULL DEFAULT NULL CHECK (json_valid(`combat_prefs`)),
  `skill_prefs` LONGTEXT NULL DEFAULT NULL CHECK (json_valid(`skill_prefs`)),
  PRIMARY KEY (obj_Id, classId)
);