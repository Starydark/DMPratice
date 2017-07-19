package JSONFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.*;


/**
 * Created by stary on 17-6-12.
 */
public class JSON {

	static int name_count;
    static String author_name;
    static boolean Train;
    static HashMap<String, success> Name = new HashMap<String, success>();
    //static HashMap<String, Integer> filename = new HashMap<String, Integer>();
    static HashMap<String, filetype> FileName = new HashMap<String, filetype>();
    static HashMap<String, Integer> map = new HashMap<String, Integer>();
    static ArrayList<Data> train_data = new ArrayList<Data>();
    //ArrayList<Data> test_data = new ArrayList<Data>();

    public static void hashMap(){
        map.put("modified", 0);
        map.put("removed", 1);
        map.put("renamed", 10);
        map.put("added", 1);
    }

    public static void find(String pathName) throws Exception {
        File dirFile =  new File(pathName);
        if(!dirFile.exists()) {
            System.out.print(dirFile + "do not exist");
            return;
        }
        System.out.println(pathName);
        
        File file1 = new File("/Users/apple/Desktop/DMoutput/result.csv");
        PrintWriter out = new PrintWriter(file1);
        //out.println("Id,Prediction");
        String[] fileList = dirFile.list();
        for(int i = 0;i < fileList.length; i++){
            String string = fileList[i];
            File file = new File(dirFile.getPath(), string);
            String name = file.getName();
            if(file.isDirectory()) {
                String train_path = file.getPath() + "/train_set.txt";
                String test_path = file.getPath() + "/test_set.txt";

                FileWriter output1 = new FileWriter(file.getPath() + "/train.txt");
                FileWriter output2 = new FileWriter(file.getPath() + "/test.txt");
                Train = true;
                readJSON(train_path, output1);
                Instances trainInstances = generateInstance();
                train_data.clear();
                //Name.clear();
                System.out.println(train_path + "finish");
                Train = false;
                readJSON(test_path, output2);
                System.out.println(test_path + "finish");
                Instances testInstances = generateInstance();
                ArrayList<Instance> testSet = generateTest(testInstances);
                model(trainInstances, testInstances, testSet, out);
                train_data.clear();
                Name.clear();
                name_count = 0;
                output1.close();
                output2.close();
            }
        }
        out.close();
        String datafile = "/Users/apple/Desktop/DMoutput/result.csv";
        String no_error_id = "/Users/apple/Desktop/DMoutput/non_error_build_ids.csv";
        Filter_Errored_BuildId(datafile, no_error_id);
    }

    public static void Filter_Errored_BuildId(String datafile, String no_error_id) throws FileNotFoundException
	{
		Scanner fp1 = new Scanner(new File(no_error_id));
		HashSet<String> No_Errored_Id_Set = new HashSet<String>();
		while(fp1.hasNext())
			No_Errored_Id_Set.add(fp1.nextLine());
		fp1.close();
		
		Scanner fp2 = new Scanner(new File(datafile));
		HashMap<String,String> result = new HashMap<String,String>();
		while(fp2.hasNext())
		{
			String[] line = fp2.nextLine().split(",");
			if(No_Errored_Id_Set.contains(line[0]))
				result.put(line[0], line[1]);
		}
		fp2.close();
		
		Random rand = new Random();
		PrintWriter output = new PrintWriter(datafile);
		output.println("Id,Prediction");
		for(Map.Entry<String, String> entry : result.entrySet())
		{
			output.println(entry.getKey() + "," + entry.getValue());
		}
		for(String id: No_Errored_Id_Set)
		{
			if(! result.keySet().contains(id))
			{
				output.println(id + "," + rand.nextInt(2));
			}
		}
		
		output.close();
	}
    
    public static void JsonCommit(JSONObject obj, FileWriter output, Data data) throws IOException, Exception{
        if(obj.isNull("author"))
            ;//output.write("author:null\r\n");
        else {
            JSONObject author = obj.getJSONObject("author");
             author_name = author.getString("name");
        }
        if(obj.has("comment_count") && obj.get("comment_count") != null) {
           // output.write("comment_count:" + obj.get("comment_count") + "\r\n");
            data.comment_count = obj.getInt("comment_count");
        }
        else
           // output.write("comment_count:0\r\n");
        	;
        if(obj.has("message")){
        	String message = obj.getString("message");
        	data.message_len += message.length();
        }
        if(obj.has("date") && obj.get("date")!= null){
            StringBuilder date = new StringBuilder(obj.getString("date").substring(0, 14));
            for(int i = 0;i < date.length(); i++){
                if(!Character.isDigit(date.charAt(i))){
                    date.deleteCharAt(i);
                }
            }
            String re = date.toString();
            data.comment_date = Integer.parseInt(re);
        }
    }

    public static void JsonArr(String name, JSONArray obj, FileWriter output, Data data) throws IOException, Exception{

        //JSONArray jsonarr = JSONArray.fromObject(obj);
        int size = obj.length();
        int additions = 0, deletions = 0;
        if(obj.isNull(0))
            return;
        String minfile = null;
        for(int i = 0; i < size; i ++){
            JSONObject object = obj.getJSONObject(i);
            if(object.has("filename") && !object.isNull("filename")){
            	String filename = object.getString("filename");
            	String[] str = filename.split("\\.");
            	String re = null;
            	if(str.length >= 1)
            		re = str[str.length - 1];
            	if(re != null && !re.equals("")) {
            		String[] strs = re.split("/");
            		re = strs[strs.length - 1];
            	}
            	if(re != null && !re.equals("")){
            		if(Train) {
            			if(FileName.containsKey(re) ){
            				filetype type = new filetype();
            				type.total = FileName.get(re).total;
            				type.success = FileName.get(re).success;
            				type.total += 1;            			
            				type.success += Integer.parseInt(data.build_result);  
            				FileName.put(re, type);
            				double suc = (double)type.success / (double) type.total;
            				if(minfile != null){
            					filetype minRe = FileName.get(minfile);
            					double filesuc = (double)minRe.success / (double)minRe.total;
            					if(suc < filesuc)
            						minfile = re;	
            				}
            				else 
            					minfile = re;
            			}	
            			else {
            				filetype type = new filetype();
            				type.total = 1;
            				type.success = Integer.parseInt(data.build_result);
            				FileName.put(re, type);
            			}
            		}
            	}
            	
            }
            if(object.has("status") && !object.isNull("status")){
                String status = (String) object.get("status");
                data.files_staus += map.get(status);
            }

            if(object.isNull("additions") && object.isNull("deletions")){
                continue;
            }
            else {
                if (!object.isNull("additions")){
                    additions += object.getInt("additions");
                }
                if (!object.isNull("deletions")) {
                    deletions += object.getInt("deletions");
                }
            }

        }
        output.write("files_addtions:" + additions + "\r\n");
        output.write("files_addtions:" + deletions + "\r\n");
        data.files_add = additions;
        data.files_del = deletions;
        if(minfile != null){
        	filetype minRe = FileName.get(minfile);
			double filesuc = (double)minRe.success / (double)minRe.total;
			data.filetype = filesuc;
			//System.out.println(data.filetype);
        }
    }

    public static void JsonCommits(JSONArray array, FileWriter output, Data data) throws IOException, Exception{
        int size = array.length();
        if(array.isNull(0)){
            output.write("commits:null\r\n");
            return;
        }
        for(int i = 0;i < size; i++){
            JSONObject object = array.getJSONObject(i);
            String sha;
            if(object.get("sha") != null) {
                sha = (String) object.getString("sha");
                StringBuilder sha2num = new StringBuilder(sha.substring(0, 7));
                for(int i1 = 0;i1 < sha2num.length(); i1++){
                    if(!Character.isDigit(sha2num.charAt(i1))){
                        char letter = sha2num.charAt(i1);
                        sha2num.deleteCharAt(i1);
                        sha2num.insert(i1, String.valueOf(letter - 'a'));
                    }
                }
                String re = sha2num.toString();
                data.sha = Integer.parseInt(re);
                output.write("commits_sha:" + sha + "\r\n");
            }
            else
                output.write("commits_sha:null\r\n");
            //System.out.println("sha: " + sha);
            JSONObject obj = object.getJSONObject("commit");
            if(i == 0 || i == 1)
            	JsonCommit(obj, output, data);

            if(object.isNull("author"))
                output.write("author:0\r\n");
            else {
                JSONObject author = object.getJSONObject("author");
                if(author.has("id")) {
                    output.write("author:" + author.get("id") + "\r\n");
                    data.author_id = author.getInt("id");
                }
                //System.out.println("author: 1");
            }
            if(object.has("stats") && !object.isNull("stats")){
                JSONObject stats = object.getJSONObject("stats");
                output.write("stats_addit:" + stats.get("additions") + "\r\n");
                output.write("stats_del:" + stats.get("deletions") + "\r\n");
                data.stats_add += stats.getInt("additions");
                data.stats_del += stats.getInt("deletions");
            }
            else
                output.write("stats_addit:null\r\n");
            if(object.has("files")){
                JSONArray files = object.getJSONArray("files");
                JsonArr("files", files, output, data);
            }
            else {
                output.write("files_addtions:0\r\n");
                output.write("files_addtions:0\r\n");
            }
        }
    }

    public static void readJSON(String name, FileWriter output) throws Exception{
        //System.out.println(name);
        String JsonContext = ReadFile.ReadFile(name);
        JSONArray jsonArray = new JSONArray(JsonContext);

        int size = jsonArray.length();
        for(int i = 0;i < size; i++){
            author_name = null;
            Data newdata = new Data();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            int build_id = jsonObject.getInt("build_id");
            newdata.build_id = build_id;
            //int build_id = (String) jsonObject.get("build_id");
            output.write("build_id: " + build_id + "\r\n");
            
            String result = (String) jsonObject.get("build_result");
            if(result.equals("?")){
                output.write("build_result\r\n");
                newdata.build_result = "?";
            }
            else if(result.equals("passed")) {
                output.write("build_result: 1\r\n");
                newdata.build_result = "0";
            }
            else
            {
            	newdata.build_result = "1";
            	output.write("build_result: 0\r\n");
            }

            JSONArray array = jsonObject.getJSONArray("commits");
            JsonCommits(array, output, newdata);
            
               
            if(author_name != null) {
                if(Name.containsKey(author_name) && Train){
                    success change = new success();
                    change.suc = Name.get(author_name).suc;
                    change.total = Name.get(author_name).total;
                    change.id = Name.get(author_name).id;
                    change.total += 1;
                    change.suc += Integer.parseInt(newdata.build_result);
                    Name.put(author_name, change);
                }
                else if(!Name.containsKey(author_name)){
                    success new_name = new success();
                    
                    if(Train) 
                    	new_name.suc = Integer.parseInt(newdata.build_result);
                    
                    else 
                    	new_name.suc = 0;
                    new_name.id = name_count++;
                    new_name.total = 1;
                    Name.put(author_name, new_name);
                }
                newdata.name = author_name;
            }
            train_data.add(newdata);
            output.write("\r\n");
            output.write("===============================");
            output.write("\r\n");
        }
        for(int i = 0;i < train_data.size(); i++){
            String a_name = train_data.get(i).name;
            if(a_name != null && !a_name.equals("")) {
            	double re = (double)Name.get(a_name).suc / (double)Name.get(a_name).total;
                train_data.get(i).success = re;
                train_data.get(i).index = Name.get(a_name).id;
                //System.out.println(a_name + " = " + re);
            }

        }

    }

    public static Instances generateInstance(){

        Attribute build_id = new Attribute("build_id");
        Attribute sha = new Attribute("sha");
        Attribute success = new Attribute("success");
        Attribute name = new Attribute("name");
        Attribute comment_date = new Attribute("comment_date");
        Attribute comment_count = new Attribute("comment_count");
        Attribute author_index = new Attribute("author_index");
        Attribute author_id = new Attribute("author_id");
        Attribute stats_add = new Attribute("stats_add");
        Attribute stats_del = new Attribute("stats_del");
        Attribute files_add = new Attribute("files_add");
        Attribute files_del = new Attribute("files_del");
        Attribute files_status = new Attribute("status");
        Attribute files_type = new Attribute("files_type");
        ArrayList<String> ResList = new ArrayList<String>(3);
        ResList.add("0");
        ResList.add("1");
        ResList.add("?");
        Attribute build_result = new Attribute("build_result", ResList);

        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(build_id);
        attributes.add(sha);
        attributes.add(success);
        attributes.add(name);
        attributes.add(comment_date);
        //attributes.add(comment_count);
        //attributes.add(author_id);
        attributes.add(author_index);
        attributes.add(stats_add);
        attributes.add(stats_del);
        attributes.add(files_add);
        attributes.add(files_del);
        attributes.add(files_status);
        attributes.add(files_type);
        attributes.add(build_result);
        
        Instances adataset = new Instances("aDataSet", attributes, train_data.size());
        adataset.setClassIndex(12);

        for(int i = 0;i < train_data.size(); i++) {
            Instance instance = new DenseInstance(13);
            instance.setDataset(adataset);
            instance.setValue(build_id, train_data.get(i).build_id);
            instance.setValue(sha, train_data.get(i).sha);
            instance.setValue(success, train_data.get(i).success);
            instance.setValue(comment_date, train_data.get(i).comment_date);
            instance.setValue(name, train_data.get(i).message_len);
            //instance.setValue(comment_count, train_data.get(i).comment_count);
            //instance.setValue(author_id, train_data.get(i).author_id);
            instance.setValue(stats_add, train_data.get(i).stats_add);
            instance.setValue(stats_del, train_data.get(i).stats_del);
            instance.setValue(files_add, train_data.get(i).files_add);
            instance.setValue(files_del, train_data.get(i).files_del);
            instance.setValue(author_index, train_data.get(i).index);
            instance.setValue(files_status, train_data.get(i).files_staus);
            instance.setValue(build_result, train_data.get(i).build_result);
            instance.setValue(files_type, train_data.get(i).filetype);
            adataset.add(instance);
        }
        return adataset;
    }

    public static ArrayList<Instance> generateTest(Instances dataSet){
        ArrayList<Instance> testSet = new ArrayList<Instance>();
        for(int i = 0;i < train_data.size();i++){
            Instance inst = new DenseInstance(13);
            inst.setDataset(dataSet);
            inst.setValue(dataSet.attribute("build_id"), train_data.get(i).build_id);
            inst.setValue(dataSet.attribute("sha"), train_data.get(i).sha);
            inst.setValue(dataSet.attribute("success"), train_data.get(i).success);
            //inst.setValue(dataSet.attribute("comment_count") ,train_data.get(i).comment_count);
            inst.setValue(dataSet.attribute("comment_date"), train_data.get(i).comment_date);
            inst.setValue(dataSet.attribute("name"), train_data.get(i).message_len);
            //inst.setValue(dataSet.attribute("author_id"), train_data.get(i).author_id);
            inst.setValue(dataSet.attribute("stats_add"), train_data.get(i).stats_add);
            inst.setValue(dataSet.attribute("stats_del"), train_data.get(i).stats_del);
            inst.setValue(dataSet.attribute("author_index"), train_data.get(i).index);
            inst.setValue(dataSet.attribute("files_add"), train_data.get(i).files_add);
            inst.setValue(dataSet.attribute("files_del"), train_data.get(i).files_del);
            inst.setValue(dataSet.attribute("status") ,train_data.get(i).files_staus);
            inst.setValue(dataSet.attribute("files_type"), train_data.get(i).filetype);
            inst.setValue(dataSet.attribute("build_result"), train_data.get(i).build_result);
            testSet.add(inst);
        }
        return testSet;
    }

    public static void model(Instances inst, Instances test, ArrayList<Instance> testSet, PrintWriter output) throws Exception {
        /*for(int i1 = 0;i1 < test.numInstances(); i1++){
            for(int j = 0;j < test.numAttributes(); j++){
                System.out.println(test.instance(i1).value(j) + "\t");
            }
            System.out.println();
        }*/
        Classifier clf = (Classifier)new J48();
        clf.buildClassifier(inst);
        Evaluation evaluation = new Evaluation(inst);
        evaluation.evaluateModel(clf, inst);
        //System.out.println(evaluation.toSummaryString());
        //System.out.println(evaluation.toMatrixString());
        //Classifier clf = (Classifier) weka.core.SerializationHelper.read("")
        for (Instance instance : testSet){
            BigDecimal bd = new BigDecimal(instance.value(0));
            String build_id = bd.toPlainString();
            //System.out.print(build_id + ", ");
            //System.out.println((int)clf.classifyInstance(instance));
            output.print(build_id + ",");
            output.println((int)clf.classifyInstance(instance));
        }
    }

    public static void main(String[] args) throws Exception{
        hashMap();
        Name.clear();
        name_count = 0;
        find("/Users/apple/Desktop/DM_course_project/");
       
    }
}
