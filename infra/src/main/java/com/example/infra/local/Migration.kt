package com.example.infra.local

import com.example.core.TestMakerColor
import io.realm.DynamicRealm
import io.realm.DynamicRealmObject
import io.realm.FieldAttribute
import io.realm.RealmMigration

/**
 * Created by keita on 2017/02/08.
 */
class Migration : RealmMigration {
    override fun migrate(realm: DynamicRealm, old: Long, newVersion: Long) {
        var oldVersion = old
        val schema = realm.schema
        if (oldVersion == 0L) {
            val personSchema = schema["Quest"]
            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema
                    ?.addField("solving", Boolean::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["solving"] = false }
            oldVersion++
        }
        if (oldVersion == 1L) {
            val personSchema = schema["Test"]
            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema
                    ?.addField("limit", Int::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["limit"] = 20 }
            oldVersion++
        }
        if (oldVersion == 2L) {
            val personSchema = schema["Quest"]
            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema
                    ?.addField("imagePath", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["imagePath"] = "" }
            personSchema!!.setNullable("imagePath", true)
            oldVersion++
        }
        if (oldVersion == 3L) {
            oldVersion++
            val cateSchema = schema.create("Cate")
            cateSchema?.addField("category", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["category"] = "フォルダー" }
                    ?.addField("color", Int::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["color"] = 0 }
        }
        if (oldVersion == 4L) {
            oldVersion++
            val personSchema = schema["Quest"]
            personSchema
                    ?.addField("explanation", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["explanation"] = "" }
        }
        if (oldVersion == 5L) {
            val personSchema = schema["Quest"]
            val selectSchema = schema["Select"]
            personSchema!!.addRealmListField("answers", selectSchema)
                    ?.transform { obj: DynamicRealmObject ->
                        if (obj.getInt("type") == 2) {
                            for (i in obj.getList("selections").indices) {
                                val answer = realm.createObject("Select")
                                answer.setString("select", obj.getList("selections")[i]!!.getString("select"))
                                obj.getList("answers").add(answer)
                            }
                        }
                    }
            oldVersion++
        }
        if (oldVersion == 6L) {
            val personSchema = schema["Quest"]
            personSchema
                    ?.addField("order", Int::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["order"] = 0 }
            oldVersion++
        }
        if (oldVersion == 7L) {
            val personSchema = schema["Quest"]
            // Change type from String to int
            personSchema
                    ?.addField("problem_temp", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject ->
                        val oldProblem = obj.getString("problem")
                        obj.setString("problem_temp", oldProblem)
                    }
                    ?.removeField("problem")
                    ?.renameField("problem_temp", "problem")
                    ?.addField("answer_temp", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject ->
                        val oldProblem = obj.getString("answer")
                        obj.setString("answer_temp", oldProblem)
                    }
                    ?.removeField("answer")
                    ?.renameField("answer_temp", "answer")
                    ?.addField("imagePath_temp", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject ->
                        val oldProblem = obj.getString("imagePath")
                        obj.setString("imagePath_temp", oldProblem)
                    }
                    ?.removeField("imagePath")
                    ?.renameField("imagePath_temp", "imagePath")
            oldVersion++
        }
        if (oldVersion == 8L) {
            val testSchema = schema["Test"]
            testSchema
                    ?.addField("startPosition", Int::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["startPosition"] = 0 }
            oldVersion++
        }
        if (oldVersion == 9L) {
            val testSchema = schema["Quest"]
            testSchema
                    ?.addField("isCheckOrder", Boolean::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["isCheckOrder"] = false }
            oldVersion++
        }
        if (oldVersion == 10L) {
            val testSchema = schema["Test"]
            testSchema
                    ?.addField("documentId", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["documentId"] = "" }
            oldVersion++
        }
        if (oldVersion == 11L) {
            val testSchema = schema["Test"]
            testSchema
                    ?.addField("order", Int::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["order"] = obj.getLong("id").toInt() }
            oldVersion++
        }
        if (oldVersion == 12L) {
            val testSchema = schema["Quest"]
            testSchema
                    ?.addField("documentId", String::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["documentId"] = "" }
            oldVersion++
        }
        if (oldVersion == 13L) {
            val personSchema = schema["Cate"]
            personSchema
                    ?.addField("order", Int::class.java, FieldAttribute.REQUIRED)
                    ?.transform { obj: DynamicRealmObject -> obj["order"] = 0 }
            oldVersion++
        }
        if (oldVersion == 14L) {
            schema.create("Category")
                    .addField("id", Long::class.java, FieldAttribute.PRIMARY_KEY)
                    .addField("name", String::class.java, FieldAttribute.REQUIRED)
                    .addField("color", Int::class.java, FieldAttribute.REQUIRED)
                    .addField("order", Int::class.java, FieldAttribute.REQUIRED)
            oldVersion++

        }
        if (oldVersion == 15L) {
            schema.rename("Test", "RealmTest")

            oldVersion++
        }
        if (oldVersion == 16L) {
            schema.rename("Category", "RealmCategory")

            oldVersion++
        }
        if (oldVersion == 17L) {
            val testSchema = schema["RealmTest"]
            testSchema
                ?.addField("source", String::class.java, FieldAttribute.REQUIRED)
                ?.transform { obj: DynamicRealmObject -> obj["source"] = "undefined" }

            oldVersion++
        }
        if (oldVersion == 18L) {
            val testSchema = schema["RealmTest"]
            testSchema
                ?.addField("themeColor", String::class.java, FieldAttribute.REQUIRED)
                ?.transform { obj: DynamicRealmObject ->
                    obj["themeColor"] = migrateColor(obj.getInt("color"))
                }

            val folderSchema = schema["RealmCategory"]
            folderSchema
                ?.addField("themeColor", String::class.java, FieldAttribute.REQUIRED)
                ?.transform { obj: DynamicRealmObject ->
                    obj["themeColor"] = migrateColor(obj.getInt("color"))
                }

            oldVersion++
        }
        //schemaVersion変えるの忘れるな(TestMakerApplication内)


    }

    private fun migrateColor(color: Int): String =
        when (color) {
            -26215 -> TestMakerColor.RED.name
            -13159 -> TestMakerColor.ORANGE.name
            -103 -> TestMakerColor.YELLOW.name
            -6684775 -> TestMakerColor.GREEN.name
            -6684724 -> TestMakerColor.TEAL.name
            -6684673 -> TestMakerColor.BLUE.name
            -6710785 -> TestMakerColor.INDIGO.name
            -26113 -> TestMakerColor.PURPLE.name
            else -> TestMakerColor.BLUE.name
        }
}