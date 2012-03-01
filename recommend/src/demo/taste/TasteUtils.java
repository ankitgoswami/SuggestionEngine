package demo.taste;

import java.util.List;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;


/**
 *
 *
 **/
public class TasteUtils {

  public static void printRecs(List<RecommendedItem> recommendations, Map<String, String> map) {
    System.out.println("Recommendations:");
    for (RecommendedItem item : recommendations) {
      Comparable<?> theItem = item.getItemID();
      String title = map.get(theItem.toString());
      System.out.println("Doc Id: " + theItem + " Title: " + title + " Score: " + item.getValue());
    }
  }

  public static void printRecsItem(List<RecommendedItem> recommendations, Map<String, String> map) {
    System.out.println("Recommendations:");
    for (RecommendedItem item : recommendations) {
      Comparable<?> theItem = item.getItemID();
      String title = map.get(theItem.toString());
      System.out.println("User: " + theItem + " Score: " + item.getValue());
    }
  }

  public static void printCommonalities(DataModel dataModel, long user, long neighbor, Map<String, String> map) throws TasteException {
    PreferenceArray userP = dataModel.getPreferencesFromUser(user);
    PreferenceArray neighP = dataModel.getPreferencesFromUser(neighbor);

    System.out.println("---------------------------");
    System.out.println("Commonalities between " + user + " and " + neighbor);
    for (Preference up : userP) {
      for (Preference np : neighP) {
        long uid = up.getItemID();
        if (uid == np.getItemID()) {
          String title = map.get(uid);
          System.out.println("ID: " + uid + " Title: " + title + " User Val: " + up.getValue() + " Neighbor Val: " + np.getValue());
        }
      }
    }
  }

  public static void printCommonalitiesItems(DataModel dataModel, long user, long neighbor, Map<String, String> map) throws TasteException {
    PreferenceArray userP = dataModel.getPreferencesFromUser(user);
    PreferenceArray neighP = dataModel.getPreferencesFromUser(neighbor);

    System.out.println("---------------------------");
    System.out.println("Commonalities between " + user + " and " + neighbor);
    for (Preference up : userP) {
      for (Preference np : neighP) {
        long uid = up.getItemID();
        if (uid == np.getItemID()) {
          System.out.println("ID: " + uid + " User Val: " + up.getValue() + " Neighbor Val: " + np.getValue());
        }
      }
    }
  }


  public static void printPreferences(DataModel dataModel, long user, Map<String, String> map) throws TasteException {
    //Iterable<Preference> prefsIter = user.getPreferences();
    PreferenceArray prefsIter = dataModel.getPreferencesFromUser(user);
    for (Preference preference : prefsIter) {
      printPref(preference, map);
    }
  }

  public static void printPref(Preference preference, Map<String, String> map) {
    Comparable<?> item = preference.getItemID();
    String title = map.get(item.toString());
    double val = preference.getValue();
    System.out.println("Title: " + title + " Rating: " + val);
  }
}
