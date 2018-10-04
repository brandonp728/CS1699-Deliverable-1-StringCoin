import java.util.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

public class StringCoin
{
    private static LinkedHashMap<String, String> coinKeyMap = null;

    public static void main(String[] args) throws IllegalArgumentException
    {
      if(args.length != 1) {
        throw new IllegalArgumentException();
      }

      File blockChainFile =  new File(args[0]);
      Scanner fileScan = null;

      try {
        fileScan = new Scanner(blockChainFile);
      } catch(FileNotFoundException e) {
        e.printStackTrace();
        System.exit(0);
      }

      Constants.loadKeys();
      coinKeyMap = new LinkedHashMap<String, String>();

      while(fileScan.hasNext()) {
        String block = fileScan.nextLine();

        if(block.contains("CREATE")) {
          create(block);
        } else {
          transfer(block);
        }
      }

      Iterator<String> coinIterator = coinKeyMap.keySet().iterator();
      Iterator<String> pkIterator = coinKeyMap.values().iterator();

      Iterator mapIterator = coinKeyMap.entrySet().iterator();
      while (mapIterator.hasNext()) {
        Map.Entry pair = (Map.Entry)mapIterator.next();
        System.out.println("Coin " + pair.getKey() + " / Owner = " + pair.getValue());
      }

    }

    private static void create(String block)
    {
      String[] blockArray = block.split(",");

      if(blockArray.length < 5) {
        System.out.println("Looks like something went wrong with the parsing\nBye!");
        System.exit(0);
      }

      String prev = blockArray[0];
      String createBlock = blockArray[1];
      String coin = blockArray[2];
      String coinsig = blockArray[3];
      String sig = blockArray[4];

      boolean verified = false;

      try {
        String blockToHash = prev + "," + createBlock + "," + coin + "," + coinsig;
        verified = PublicKeyDemo.verify(blockToHash, PublicKeyDemo.convertHexToBytes(sig), Constants.billPk);
      } catch(Exception e) {
        System.out.println("Error verifying the signature");
        e.printStackTrace();
        System.exit(0);
      }

      if(verified) {
        coinKeyMap.put(coin, Constants.billPkString);
      } else {
        System.out.println("Error verifying the signature");
        System.exit(0);
      }
    }

    private static void transfer(String block)
    {
      String[] blockArray = block.split(",");

      if(blockArray.length < 5) {
        System.out.println("Looks like something went wrong with the parsing\nBye!");
        System.exit(0);
      }

      String prev = blockArray[0];
      String transferBlock = blockArray[1];
      String coin = blockArray[2];
      String pk = blockArray[3];
      String sig = blockArray[4];

      PublicKey newOwner = null;
      try {
        newOwner = PublicKeyDemo.loadPublicKey(coinKeyMap.get(coin));
      } catch(Exception e) {
        System.out.println("Error loading key in transfer");
        System.out.println(e);
        System.exit(0);
      }

      boolean verified = false;

      try {
        String blockToHash = prev + "," + transferBlock + "," + coin + "," + pk;
        verified = PublicKeyDemo.verify(blockToHash, PublicKeyDemo.convertHexToBytes(sig), newOwner);
      } catch(Exception e) {
        System.out.println("Error verifying the signature");
        e.printStackTrace();
        System.exit(0);
      }

      if(verified) {
        coinKeyMap.replace(coin, coinKeyMap.get(coin), pk);
      }


    }
}
