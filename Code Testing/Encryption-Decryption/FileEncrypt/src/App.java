import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

// import com.ttv.facerecog.CryptoUtils;

public class App {
    public static void main(String[] args) throws Exception {
        try {
            String fileNme = "13";
            String fileExt = ".xlsx";
            // D:\2\the-amazing-app\Code Testing\Encryption-Decryption\FileEncrypt\files\e.txt
            // D:\2\the-amazing-app\Code Testing\Encryption-Decryption\FileEncrypt\files\Encrypted\t.txt
            String path = "D:/2/the-amazing-app/Code Testing/Encryption-Decryption/FileEncrypt/files/";
            String fileScrP = path + fileNme+fileExt;
            String fileEncP = path + "/Encrypted/" + fileNme+".bin";
            String fileDecP = path + "/Dycrypted/"+ fileNme+fileExt;
            String key =  "Mary has one cat";
            File fileScr = new File(fileScrP);
            File fileEnc = new File(fileEncP);
            File fileDec = new File(fileDecP);
            
            // fileEnc.createNewFile();
            fileDec.createNewFile();
            System.out.println("Original File: "+ fileScr.getName());
         
            try {
                // Encryption
                CryptoUtils.encrypt(key, fileScr, fileEnc);  
                System.out.println("Encrypted File : "+ fileEnc.getName());
       
                // the source and the encrypted file
                long i = Files.mismatch(fileScr.toPath(), fileDec.toPath());
                // System.out.println(i);
                if(i==0) // if not same sucess
                System.out.println("The Source and the Encrypted File are not same. File Sucessefully Encrypted!");
                else
                System.out.println("The Source and the Encrypted File are same. File couldn't get Encrypted!");

                // Decryption
                CryptoUtils.decrypt(key, fileEnc, fileDec);
                System.out.println("Decrypted File : "+ fileDec.getName());

                // the source and the decrypted file
                i = Files.mismatch(fileScr.toPath(), fileDec.toPath());
                // System.out.println(i);
                if(i!=0) // if same sucess
                System.out.println("The Source and the Decrypted File are same. File Sucessefully Decrypted!");
                else
                System.out.println("The Source and the Decrypted File are same. File couldn't get Decrypted!");


            } catch (CryptoException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            }
        
            
            // Scanner sc = new Scanner(new File(filePath));
            // String ts = "...";
            // StringBuilder sb = new StringBuilder();

            // while(sc.hasNext()){
            //     sb.append(sc.nextLine());
            // }

            // System.out.println(sb.toString());

            // File file = 
        } catch (Exception e) {
            // TODO: handle exception 
            // D:\2\the-amazing-app\Code Testing\Encryption-Decryption\FileEncrypt\src\e.txt
                        System.out.println(e.getMessage());
        }
    }
}
