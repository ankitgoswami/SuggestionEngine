package demo.taste;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 *
 **/
public class GenerateRatings {


  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

    File manual = new File(args[0]);
    File out = new File(args[1]);
    File docIds = new File(args[2]);
    Writer writer = new FileWriter(out);
    BufferedReader reader = new BufferedReader(new FileReader(manual));
    writer.write("#Automatically generated ratings for Wikipedia, combined with some manually generated ratings.\n");
    writer.write("#The recommendations, format is: user id, item id, preference value\n");

    String line = null;
    //Write out the manually created items
    while ((line = reader.readLine()) != null) {
      if (line.startsWith("#") == false) {
        String[] splits = line.split(",");
        //second item is the item id
        writer.write(line);
        writer.write('\n');
      }
    }
    writer.write("#Begin Auto generated\n");
    InputSource is = new InputSource(new FileInputStream(docIds));
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(false);
    SAXParser sp = factory.newSAXParser();
    Random random = new Random(0);//try the same seed for now
    DefaultHandler handler = new DocIdContentHandler(writer, random);
    //write out the randomly generated ratings
    sp.parse(is, handler);
    writer.flush();
    writer.close();
  }

  static class DocIdContentHandler extends DefaultHandler {
    private boolean inDocId;
    private StringBuilder builder = new StringBuilder();
    private Writer writer;
    private Random random;

    DocIdContentHandler(Writer writer, Random random) {
      this.writer = writer;
      this.random = random;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (qName.equals("str") && attributes.getValue("name") != null && attributes.getValue("name").equals("docid")){
        inDocId = true;
      }

    }

    @Override
    public void characters(char[] chars, int offset, int len) throws SAXException {
      if (inDocId == true){
        builder.append(chars, offset, len);
      }
    }

    @Override
    public void endElement(String uri, String local, String qName) throws SAXException {
      if (inDocId == true){
        try {

          String itemId = builder.toString();
          //Randomly rate the item for some random number of users
          Set<String> seenUsers = new HashSet<String>();
          int numUsers = random.nextInt(100);
          for (int i = 0; i < numUsers; i++){
            //Pick a user out of the 990 available
            String userId = String.valueOf(random.nextInt(990));

            if (seenUsers.contains(userId) == false) {
              seenUsers.add(userId);
              writer.write(userId);
              writer.write(',');
              writer.write(itemId);
              writer.write(',');
              float pref = random.nextFloat() * 5;
              if (random.nextBoolean() == false){//like or dislike
                pref = -pref;
              }
              writer.write(String.valueOf(pref));
              writer.write('\n');
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      builder.setLength(0);
      inDocId = false;
    }
  }

}
