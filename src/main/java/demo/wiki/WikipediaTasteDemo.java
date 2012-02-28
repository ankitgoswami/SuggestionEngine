package demo.wiki;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.ConnectionPoolDataSource;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.se.datamodel.JDBCDataModelImpl;

public class WikipediaTasteDemo {


  public static void main(String[] args) throws IOException, TasteException, SAXException, ParserConfigurationException {
    String docIdsTitle = "src/main/resources/docIdsTitles.xml";
    long userId = 995;
    //Integer neighbors = Integer.parseInt(args[2]);
    InputSource is = new InputSource(new FileInputStream(docIdsTitle));
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    SAXParser sp = factory.newSAXParser();
    WikiContentHandler handler = new WikiContentHandler();
    sp.parse(is, handler);

    MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
	dataSource.setDatabaseName("se");
	dataSource.setUser("root");
	dataSource.setPassword("");
	dataSource.setCachePreparedStatements(true);
	dataSource.setCachePrepStmts(true);
	dataSource.setCacheResultSetMetadata(true);
	dataSource.setAlwaysSendSetIsolation(false);
	dataSource.setElideSetAutoCommits(true);
    //create the data model
    DataModel dataModel = new JDBCDataModelImpl(new ConnectionPoolDataSource(dataSource));
    //Create an ItemSimilarity
    ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
    //Create an Item Based Recommender
    ItemBasedRecommender recommender =
            new GenericItemBasedRecommender(dataModel, itemSimilarity);
    //Get the recommendations
    List<RecommendedItem> recommendations =
            recommender.recommend(userId, 5);
    TasteUtils.printRecs(recommendations, handler.map);

  }

}