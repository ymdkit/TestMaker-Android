package jp.gr.java_conf.foobar.testmaker.service.models;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager;

/**
 * Created by keita on 2017/02/08.
 */

public class RealmController {

    private Context context;
    private Realm realm;

    private SharedPreferenceManager sharedPreferenceManager;


    public RealmController(Context c, RealmConfiguration config) {

        context = c;

        realm = Realm.getInstance(config);

        sharedPreferenceManager = new SharedPreferenceManager(c);

    }

    private ArrayList<Test> getList() {

        final RealmResults<Test> realmArray;

        switch (sharedPreferenceManager.getSort()) {
            case -1:

                realmArray = realm.where(Test.class).findAll().sort("title");

                break;
            case 0:

                realmArray = realm.where(Test.class).findAll().sort("title");
                break;
            case 1:

                realmArray = realm.where(Test.class).findAll().sort("title", Sort.DESCENDING);
                break;
            case 2:

                realmArray = realm.where(Test.class).findAll().sort("history", Sort.DESCENDING);
                break;

            default:
                realmArray = realm.where(Test.class).findAll().sort("title");
                break;

        }

        return new ArrayList<>(realmArray);
    }

    public Test getTest(long testId) {
        return realm.where(Test.class).equalTo("id", testId).findFirst();
    }

    public void addTest(String title, int color, String category) {

        realm.beginTransaction();

        // 初期化
        long nextUserId = 1;
        // userIdの最大値を取得
        Number maxUserId = realm.where(Test.class).max("id");
        // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
        if (maxUserId != null) {
            nextUserId = maxUserId.intValue() + 1;
        }

        Test test = realm.createObject(Test.class, nextUserId);

        test.setTitle(title);
        test.setColor(color);
        test.setCategry(category);
        test.setLimit(100);

        realm.commitTransaction();
    }

    public void updateTest(Test test, String title, int color, String category) {
        realm.beginTransaction();

        test.setTitle(title);
        test.setColor(color);
        test.setCategry(category);

        realm.commitTransaction();
    }

    public void deleteTest(Test test) {

        realm.beginTransaction();

        test.deleteFromRealm();

        realm.commitTransaction();
    }


    public void updateHistory(Test test) {
        realm.beginTransaction();

        test.setHistory();

        realm.commitTransaction();
    }

    public void updateLimit(Test test, int limit) {

        realm.beginTransaction();

        test.setLimit(limit);

        realm.commitTransaction();

    }

    public void addCate(String category, int color) {

        realm.beginTransaction();

        Cate cate = realm.createObject(Cate.class);

        cate.setCategory(category);
        cate.setColor(color);

        realm.commitTransaction();

    }

    public ArrayList<Cate> getCateList() {

        final RealmResults<Cate> realmArray;

        switch (sharedPreferenceManager.getSort()) {
            case -1:

                realmArray = realm.where(Cate.class).findAll().sort("category");

                break;
            case 0:

                realmArray = realm.where(Cate.class).findAll().sort("category");
                break;
            case 1:

                realmArray = realm.where(Cate.class).findAll().sort("category", Sort.DESCENDING);
                break;
            case 2:

                realmArray = realm.where(Cate.class).findAll().sort("category");
                break;

            default:
                realmArray = realm.where(Cate.class).findAll().sort("category");
                break;

        }

        return new ArrayList<>(realmArray);
    }

    public void deleteCate(Cate cate) {

        realm.beginTransaction();

        cate.deleteFromRealm();

        realm.commitTransaction();
    }

    public Quest getQuestion(long testId, int position) {

        return realm.where(Test.class).equalTo("id", testId).findFirst().getQuestions().get(position);
    }


    public ArrayList<Quest> getQuestions(long testId) {

        final RealmList<Quest> realmArray = getTest(testId).getQuestions();

        return new ArrayList<>(realmArray);
    }

    public ArrayList<Quest> getFilterQuestions(long testId, String filter) {

        ArrayList<Quest> array = new ArrayList<>();

        final RealmList<Quest> realmArray = getTest(testId).getQuestions();

        for (Quest quest : realmArray) {

            if (quest.getProblem().contains(filter)) {
                Log.d(quest.getProblem(), "");
                array.add(quest);

            } else if (quest.getAnswer().contains(filter)) {
                array.add(quest);

            } else if (quest.getExplanation().contains(filter)) {
                array.add(quest);

            }

        }

        return array;

    }

    public ArrayList<Quest> getQuestionsSolved(long testId) {

        ArrayList<Quest> array = new ArrayList<>();

        final RealmList<Quest> realmArray = getTest(testId).getQuestions();

        for (Quest quest : realmArray) {

            if (quest.getSolving()) {
                array.add(quest);
            }
        }

        return array;
    }

    public void addQuestion(long testId, StructQuestion problem, long questionId) {

        realm.beginTransaction();

        Test test = realm.where(Test.class).equalTo("id", testId).findFirst();

        Quest question;

        if (questionId != -1) {

            question = realm.where(Quest.class).equalTo("id", questionId).findFirst();

            if (question == null) {
                Toast.makeText(context, context.getString(R.string.msg_already_delete), Toast.LENGTH_SHORT).show();

                realm.commitTransaction();

                return;
            }


        } else {
            // 初期化
            long nextUserId;
            nextUserId = 1;
            // userIdの最大値を取得
            Number maxUserId = realm.where(Quest.class).max("id");
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxUserId != null) {
                nextUserId = maxUserId.intValue() + 1;
            }

            question = realm.createObject(Quest.class, nextUserId);
            test.getQuestions().add(question);
        }

        question.setExplanation(problem.explanation);
        question.setType(problem.type);
        question.setProblem(problem.question);
        question.setAnswer(problem.answer);
        question.setSelections(problem.others);
        question.setCorrect(false);
        question.setAuto(problem.auto);

        if (question.getImagePath() != null) {
            if (!question.getImagePath().equals(problem.imagePath)) {
                context.deleteFile(question.getImagePath());
            }
        }
        question.setImagePath(problem.imagePath);

        realm.commitTransaction();

        Toast.makeText(context, context.getString(R.string.msg_save), Toast.LENGTH_LONG).show();

    }

    public void deleteQuestion(Quest question) {

        realm.beginTransaction();

        question.deleteFromRealm();

        realm.commitTransaction();

    }

    public void updateCorrect(Quest quest, boolean correct) {

        realm.beginTransaction();

        quest.setCorrect(correct);

        realm.commitTransaction();

    }

    public void updateSolving(ArrayList<Quest> questions, boolean solving) {
        realm.beginTransaction();

        for (Quest question : questions) {

            question.setSolving(solving);
        }

        realm.commitTransaction();
    }

    public void updateSolving(Quest question, boolean solving) {
        realm.beginTransaction();

        question.setSolving(solving);

        realm.commitTransaction();
    }


    public void close() {

        realm.close();

    }

    public void convert(StructTest structTest, long testId) {

        realm.beginTransaction();

        // 初期化
        Integer nextUserId = 1;
        // userIdの最大値を取得
        Number maxUserId = realm.where(Test.class).max("id");
        // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
        if (maxUserId != null) {
            nextUserId = maxUserId.intValue() + 1;
        }

        Test test; // Create managed objects directly

        if (testId != -1) {

            test = getTest(testId);
            test.setQuestions(new RealmList<>());

        } else {

            test = realm.createObject(Test.class, nextUserId); // Create managed objects directly

        }

        test.setTitle(structTest.getTitle());
        test.setColor(structTest.getColor());
        test.setCategry(structTest.getCategory());
        test.setHistory(structTest.getHistory());
        test.setLimit(100);

        for (int j = 0; j < structTest.getProblems().size(); j++) {

            // 初期化
            Integer nextQuestId = 1;
            // userIdの最大値を取得
            Number maxQuestId = realm.where(Quest.class).max("id");
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxQuestId != null) {
                nextQuestId = maxQuestId.intValue() + 1;
            }

            Quest q = realm.createObject(Quest.class, nextQuestId);

            q.setProblem(structTest.getProblems().get(j).question);
            q.setAnswer(structTest.getProblems().get(j).answer);
            q.setAuto(structTest.getProblems().get(j).auto);
            q.setType(structTest.getProblems().get(j).type);
            q.setSelections(structTest.getProblems().get(j).others);
            q.setExplanation(structTest.getProblems().get(j).explanation);
            q.setImagePath("");

            test.getQuestions().add(q);
        }

        realm.commitTransaction();

    }


    public ArrayList<Test> getCategorizedList(String category) {

        ArrayList<Test> array = new ArrayList<>();

        final RealmResults<Test> realmArray;

        switch (sharedPreferenceManager.getSort()) {
            case -1:

                realmArray = realm.where(Test.class).findAll().sort("title");

                break;
            case 0:

                realmArray = realm.where(Test.class).findAll().sort("title");
                break;
            case 1:

                realmArray = realm.where(Test.class).findAll().sort("title", Sort.DESCENDING);
                break;
            case 2:

                realmArray = realm.where(Test.class).findAll().sort("history", Sort.DESCENDING);
                break;

            default:
                realmArray = realm.where(Test.class).findAll().sort("title");
                break;

        }

        for (Test test : realmArray) {
            if (test.getCategory().equals(category)) {
                array.add(test);
            }
        }

        return array;

    }

    public List<Object> getMixedList() {

        List<Object> items = new ArrayList<>();

        for (Cate cate : getCateList()) {
            for (Test test : getList()) {
                if (cate.getCategory().equals(test.getCategory())) {
                    items.add(cate);
                    break;
                }
            }
        }

        outside:
        for (Test test : getList()) {

            for (Cate cate : getCateList()) {
                if (cate.getCategory().equals(test.getCategory())) {

                    continue outside;

                }
            }

            items.add(test);

        }

        return items;
    }

}
