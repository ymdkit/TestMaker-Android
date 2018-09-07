package jp.gr.java_conf.foobar.testmaker.service.models;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

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

        //schemaVersion変えるの忘れるな(MyApplication内)

    }
}
