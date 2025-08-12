package org.example;

import jep.SharedInterpreter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpResponse;

public class tesseractJEP {
    public static String captchaExtract(HttpResponse<String> httpResponse) throws IOException {
        Document document = Jsoup.parse((String) httpResponse.body());
        Elements captchaElement = document.selectXpath("/html/body/form/table/tbody/tr[8]/td/font/div/img[1]");
        String captchaSrc = captchaElement.getFirst().attr("src");
        String captchaAnswer;
        URL url = new URL("https://www.imsnsit.org/imsnsit/" + captchaSrc);
        BufferedImage image = ImageIO.read(url);
        File outputFile = new File(captchaSrc);
        ImageIO.write(image, "jpg", outputFile);
        try (SharedInterpreter pyinterp = new SharedInterpreter()) {
            pyinterp.exec("import pytesseract");
            pyinterp.exec("from PIL import Image");
            pyinterp.set("image_path", captchaSrc);
            pyinterp.exec("pytesseract.pytesseract.tesseract_cmd = r'C:\\Program Files\\Tesseract-OCR\\tesseract.exe'");
            pyinterp.exec("img = Image.open(image_path)");
            pyinterp.exec("custom_lang = 'mydigits'");
            pyinterp.exec("tessdata_dir_config = '--tessdata-dir tessdata --psm 11'");
            pyinterp.exec("captcha = pytesseract.image_to_string(img, lang=custom_lang, config=tessdata_dir_config)");
            //System.out.println(pyinterp.getValue("captcha").getClass());
            //System.out.println(pyinterp.getValue("captcha"));
            captchaAnswer = pyinterp.getValue("captcha").toString();
        }
        outputFile.delete();
        return captchaAnswer;
    }
}
