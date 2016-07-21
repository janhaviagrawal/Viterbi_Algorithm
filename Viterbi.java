/*
 * Viterbi algorithm implementation for Parts of speech tagging.
 */
package Viterbi;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author janhaviagrawal
 */
public class Viterbi {
    public static void main(String[] args) throws FileNotFoundException, IOException 
    {
        String s1 = "text file path";
        File folder = new File(s1);
        File[] listOfFiles = folder.listFiles();
        Viterbi class1 = new Viterbi();
       
        HashMap<String,Integer> word_cat = new HashMap<String,Integer>();
        HashMap<String,Integer> cat_cat = new HashMap<String,Integer>();
        HashMap<String,Integer> cat_occurence = new HashMap<String,Integer>();
        HashMap<String,Integer> cat_start = new HashMap<String,Integer>();
        
        ArrayList<String> train_words = new ArrayList<String>();
        ArrayList<String> train_words_temp = new ArrayList<String>();
        ArrayList<String> train_labels = new ArrayList<String>();
        ArrayList<String> test_words = new ArrayList<String>();
        ArrayList<String> test_labels = new ArrayList<String>();
        
        int j=0;
        
        int counter=0,count=0,testwords=0;
        
        //Training starts here. Each sentence is read and divided into words and labels and sent to the relevant functions populating the hashmaps declared above
        for (File file : listOfFiles)
        {
            if (file.isFile()) 
            {
              String fileName = s1 + "/" +   file.getName();
	      BufferedReader reader = new BufferedReader(new FileReader(fileName));
              String line = reader.readLine();
              while(line!=null)
              {
                  j=0;
                  if(!line.isEmpty())
                  {
                      String[] tempBuffer = line.split(" ");    
                      while(j<tempBuffer.length)
                      {
                         if(tempBuffer[j].contains("/"))
                         {
                           
                           String[] word_tag = tempBuffer[j].split("/");
                           train_words_temp.add(tempBuffer[j]);
                           train_words.add(word_tag[0]);
                           train_labels.add(word_tag[word_tag.length-1]);
                           if(word_tag[0].equals(".") || word_tag[0].equals(")"))
                           {

                               if(cat_start.get(train_labels.get(0))!=null)
                                         cat_start.put(train_labels.get(0), cat_start.get(train_labels.get(0))+1);
                               else
                                        cat_start.put(train_labels.get(0), 1);
                               
                               word_cat = class1.getWordCategory(train_words_temp,word_cat);
                               cat_occurence = class1.getCatOccurence(train_labels,cat_occurence);
                               
                                cat_cat = class1.getCategoryCategory(train_labels,cat_cat);   
                               }
                               train_words.clear();
                               train_labels.clear();
                               train_words_temp.clear();
                               counter++;
                            }  
                         }
                         j++;
                     }
                  } 
                  line=reader.readLine();
              }
            }
        }  
        
        //Removing gibberish values from category count hashmap
        HashMap<String,Integer> copy1 = (HashMap) cat_occurence.clone();
        Iterator iterator = copy1.entrySet().iterator();
        while(iterator.hasNext())
        {
            Map.Entry<String,Integer> pair = (Map.Entry<String,Integer>) iterator.next();
            if(pair.getValue()==1 )
            {
                cat_occurence.remove(pair.getKey());
            }
        }
        
        //Creating an list of all possible tags in the training data
         Iterator categories = cat_occurence.entrySet().iterator();
         String[] categories_name = new String[cat_occurence.keySet().size()];
         int i=0;
         while(categories.hasNext())
        {
            Map.Entry<String,Integer> pair = (Map.Entry)categories.next();
            categories_name[i] = pair.getKey();
            i++;     
        }
        
         //Testing with cross validation
       //  int cv=0;
        for(int cv=0;cv<=9;cv++)
        {       
        j=0;
        counter=0;count=0;testwords=0;
        for (File file : listOfFiles)
        {
            if (file.isFile()) 
            {
              String fileName = s1 + "/" +   file.getName();
	      BufferedReader reader = new BufferedReader(new FileReader(fileName));
              String line = reader.readLine();
              while(line!=null)
              {
                  j=0;
                  if(!line.isEmpty())
                  {
                      String[] tempBuffer = line.split(" ");    
                      while(j<tempBuffer.length)
                      {
                          if(counter>end_index[cv])
                              break;
                         if(tempBuffer[j].contains("/"))
                         {
                           String[] word_tag = tempBuffer[j].split("/");
                           if(counter>=start_index[cv] && counter<=end_index[cv])
                           {
                                   test_words.add(word_tag[0]);
                                   test_labels.add(word_tag[word_tag.length-1]);
                                   testwords++;
                            
                           }
                           if(word_tag[0].equals(".") || word_tag[0].equals(")"))
                           {
                              if(test_words.size()!=0)
                               {
                                   String[] testWords = new String[test_words.size()];
                                   testWords = test_words.toArray(testWords);
                                   String[] final_result = class1.viterbi(categories_name,cat_start,cat_cat,word_cat,cat_occurence,testWords);
                               
                                   for(i=0;i<final_result.length;i++)
                                    {
                                       
                                        if(final_result[i].equals(test_labels.get(i)))
                                        {
                                             count++;
                                        }                                       
                                    }
                            
                                    test_words.clear();
                                    test_labels.clear();
                                }
                               
                               counter++;
                           }
                         }
                         j++;
                     }
                  } 
                 
                  line=reader.readLine();
              }
            }
        }
        System.out.println("Cross validation count " +cv + " " +"Accuracy " + (double)count/testwords);
    }
    }  
    
    
    /*@getCategoryCategory function calculates the count of each biagram in the training data
       @param - train_labels contains the POS tags for words in a sentence
              -cat_cat contains the count of bigrams from previous sentences or is empty for first sentence
    */
    public HashMap<String,Integer> getCategoryCategory(ArrayList<String> train_labels,HashMap<String,Integer> cat_cat)
    {
        int i=0;
        String[] prev_cat={};
        while(i<train_labels.size())
        {          
           String[] word_tag1 = train_labels.get(i).split("\\|");
           for(int l=0; l<word_tag1.length;l++ )
           {
              for(int k=0;k<prev_cat.length;k++)
                {
                    
                  String cat1_cat2 = word_tag1[l] + "_" + prev_cat[k];                                      
                  if(cat_cat.get(cat1_cat2)!=null)
                  {
                    cat_cat.put(cat1_cat2,cat_cat.get(cat1_cat2)+1);
                  }
                  else
                  {
                    cat_cat.put(cat1_cat2,1);
                  }                                      
                }                 
           }
           prev_cat = word_tag1;
            i++;
        }
                                   
        return cat_cat;
    }
    
    /*@getWordCategory function calculates the count of each W|cat in the training data
       @param - train_word_temp contains the words and POS tags in the form word/POS
              -word_cat contains the count of W|cat from previous sentences or is empty for first sentence
    */
    public HashMap<String,Integer> getWordCategory(ArrayList<String> train_words_temp,HashMap<String,Integer> word_cat)
    {
        int i=0;
        while(i<train_words_temp.size())
        {
           String word_tag = train_words_temp.get(i);
                 if(word_cat.get(word_tag)!=null)
                 {
                    word_cat.put(word_tag, word_cat.get(word_tag)+1);
                 }
                 else
                 {
                   word_cat.put(word_tag,1);
                 }
            i++;
        }
        return word_cat;
    }
    
    /*@getCatOccurence function calculates the count of each POS tag in the training data
       @param - train_labels contains the POS tags for words in a sentence
              -cat_occurence contains the count of POS Tags from previous sentences or is empty for first sentence
    */
    public HashMap<String,Integer> getCatOccurence(ArrayList<String> train_labels,HashMap<String,Integer> cat_occurence) 
    {
        int i=0;
        while(i<train_labels.size())
        {
            String[] word_tag1 = train_labels.get(i).split("\\|");       
            int k=0;
            while(k<word_tag1.length)
            {
                if(cat_occurence.get(word_tag1[k])!=null)
                {
                    cat_occurence.put(word_tag1[k], cat_occurence.get(word_tag1[k])+1);
                }
                else
                {
                    cat_occurence.put(word_tag1[k],1);
                }
                k++;
            }
            i++;
           }      
           
        return cat_occurence;
    }
    
    /*@getTotalValues function calculates the sum of values for a given hashmap
       @param - cat_occurence -- the given hashmap
    */
    public int getTotalValues(HashMap<String,Integer> cat_occurence)
    {
        int sum = 0;
        for (int f : cat_occurence.values()) 
        {
            sum += f;
        }
        return sum;
    }
    
    /*@Viterbi function predicts the tag for words in the test data
       @param - categories_name contains the name of each POS tag which appears in the training data
              - cat_start is the hashmap that contains the count of each tag appeparing at the start of the sentence in the training data
              - cat_cat contains the count of bigrams from training data
              - word_cat contains the count of W|Cat from training data
              - cat_occurence contains the count of each tag from trainning data
              - Observation contains the test words
    */
    public String[] viterbi(String[] categories_name,HashMap<String,Integer> cat_start,HashMap<String,Integer> cat_cat, HashMap<String,Integer> word_cat, HashMap<String,Integer> cat_occurence,String[] observation)
    {
       
        double[][] Viterbi_probabilities = new double[observation.length][cat_occurence.keySet().size()];
        int[][] backPointer = new int[observation.length][cat_occurence.keySet().size()];
       
        String[] result = new String[observation.length];
        int i=0;
       
        i=0;
        while (i<categories_name.length)
        {          
           double category_probability = (double) 0.001/getTotalValues(cat_start);
           double observation_probability=(double) 0.001/(getTotalValues(cat_occurence) + (cat_occurence.get(categories_name[i])));
           if(cat_start.get(categories_name[i])!=null)
               category_probability = (double)(cat_start.get(categories_name[i]))/getTotalValues(cat_start);
           if(word_cat.get(observation[0]+"/"+categories_name[i])!=null){
              observation_probability = (double)(word_cat.get(observation[0]+"/"+categories_name[i]))/(getTotalValues(cat_occurence)+(cat_occurence.get(categories_name[i])));}
            Viterbi_probabilities[0][i] =(double) category_probability* observation_probability;
            backPointer[0][i] = 0;
            i++;
        }
        i =1;
        while(i<observation.length)
        {  
          int t=0;
          while (t<categories_name.length)
           {
              String cat_main = categories_name[t];
              double cat_main_value = (double)cat_occurence.get(categories_name[t]);
              double temp;
              int k=0;
              while(k<categories_name.length)
              {
                  String cat_temp = categories_name[k];
                  double cat_temp_value =(double) cat_occurence.get(categories_name[k]);
                  double transition_probability =(double) 0.001/(getTotalValues(cat_occurence)+cat_temp_value);
                  double observation_probability = (double) 0.001/(getTotalValues(cat_occurence)+cat_main_value);
                  if(cat_cat.get(cat_main+"_"+cat_temp)!=null){
                      transition_probability = (double)(cat_cat.get(cat_main+"_"+cat_temp))/(cat_main_value+(cat_temp_value));} 
                  if(word_cat.get(observation[i]+"/"+cat_main)!=null)
                     observation_probability = (double)(word_cat.get(observation[i]+"/"+cat_main))/(getTotalValues(cat_occurence)+(cat_main_value));
                  temp = (double)Viterbi_probabilities[i-1][k]*transition_probability*observation_probability;
                  if(temp>Viterbi_probabilities[i][t])
                  {
                      Viterbi_probabilities[i][t] = (double)temp;
                      backPointer[i][t] = k;
                  }
                  k++;
              }
             
              t++;
           }
           i++;
         }
        i=0;
        double temp_max =0;
        int index=6;
        for(i=0;i<cat_occurence.keySet().size();i++)
        {
            if(Viterbi_probabilities[observation.length-1][i]>temp_max)
            {
                temp_max = Viterbi_probabilities[observation.length-1][i];
                index =i;
            }
        }
        result[observation.length-1] = categories_name[index];
        for(i =observation.length-2; i>=0;i--)
        {
            result[i] = categories_name[backPointer[i+1][index]];
            index = backPointer[i+1][index];
        }
        return result;
        }
}
