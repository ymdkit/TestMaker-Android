package jp.gr.java_conf.foobar.testmaker.service.models;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import jp.gr.java_conf.foobar.testmaker.service.Constants;

/**
 * Created by keita on 2017/02/08.
 */
public class Migration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            RealmObjectSchema personSchema = schema.get("Quest");

            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema
                    .addField("solving", Boolean.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("solving", false));

            oldVersion++;
        }

        if (oldVersion == 1) {

            RealmObjectSchema personSchema = schema.get("Test");

            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema
                    .addField("limit", Integer.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("limit", 20));

            oldVersion++;

        }

        if (oldVersion == 2) {

            RealmObjectSchema personSchema = schema.get("Quest");

            // Combine 'firstName' and 'lastName' in a new field called 'fullName'
            personSchema
                    .addField("imagePath", String.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("imagePath", ""));

            personSchema.setNullable("imagePath", true);

            oldVersion++;

        }

        if (oldVersion == 3) {

            oldVersion++;

            RealmObjectSchema cateSchema = schema.create("Cate");


            cateSchema.addField("category", String.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("category", "カテゴリー"))
                    .addField("color", Integer.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("color", 0));


        }

        if (oldVersion == 4) {
            oldVersion++;

            RealmObjectSchema personSchema = schema.get("Quest");

            personSchema
                    .addField("explanation", String.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("explanation", ""));

        }

        if (oldVersion == 5) {

            RealmObjectSchema personSchema = schema.get("Quest");

            RealmObjectSchema selectSchema = schema.get("Select");


            personSchema.addRealmListField("answers",selectSchema)
                    .transform(obj -> {
                        if (obj.getInt("type") == Constants.COMPLETE) {

                            for(int i=0;i<obj.getList("selections").size();i++){
                                DynamicRealmObject answer = realm.createObject("Select");
                                answer.setString("select",obj.getList("selections").get(i).getString("select"));
                                obj.getList("answers").add(answer);
                            }


                        }
                    });


            oldVersion++;

        }

        if (oldVersion == 6) {

            RealmObjectSchema personSchema = schema.get("Quest");

            personSchema
                    .addField("order", Integer.class, FieldAttribute.REQUIRED)
                    .transform(obj -> obj.set("order", 0));


            oldVersion++;

        }


        if (oldVersion == 7) {

            RealmObjectSchema personSchema = schema.get("Quest");

            // Change type from String to int
            personSchema
                    .addField("problem_temp", String.class,FieldAttribute.REQUIRED)
                    .transform(obj -> {
                        String oldProblem = obj.getString("problem");
                        obj.setString("problem_temp",oldProblem);
                    })
                    .removeField("problem")
                    .renameField("problem_temp", "problem")
                    .addField("answer_temp", String.class,FieldAttribute.REQUIRED)
                    .transform(obj -> {
                        String oldProblem = obj.getString("answer");
                        obj.setString("answer_temp",oldProblem);
                    })
                    .removeField("answer")
                    .renameField("answer_temp", "answer")
                    .addField("imagePath_temp", String.class,FieldAttribute.REQUIRED)
                    .transform(obj -> {
                        String oldProblem = obj.getString("imagePath");
                        obj.setString("imagePath_temp",oldProblem);
                    })
                    .removeField("imagePath")
                    .renameField("imagePath_temp", "imagePath");


            oldVersion++;

        }


        //schemaVersion変えるの忘れるな(MyApplication内)

    }
}
