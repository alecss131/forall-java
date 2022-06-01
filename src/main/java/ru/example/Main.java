package ru.example;

import com.lowagie.text.DocumentException;
import lombok.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setSuffix(".html");
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        Context context = new Context();
        List<Data> data = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:data.db");
            String sql = "create table if not exists data(id integer primary key not null, a text, b integer, c real)";
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
                sql = "select * from data";
                ResultSet result = statement.executeQuery(sql);
                while (result.next()) {
                    data.add(new Data(result.getString("a"), result.getInt("b"), result.getFloat("c")));
                }
            }
            if (data.isEmpty()) {
                sql = "insert into data('a', 'b', 'c') values(?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    Random rnd = new Random();
                    String[] names = {"data", "another data", "test", "some data", "string"};
                    for (String name : names) {
                        statement.setObject(1, name);
                        statement.setObject(2, rnd.nextInt());
                        statement.setObject(3, rnd.nextFloat());
                        statement.execute();
                    }
                }
                try (Statement statement = connection.createStatement()) {
                    sql = "select * from data";
                    ResultSet result = statement.executeQuery(sql);
                    while (result.next()) {
                        data.add(new Data(result.getString("a"), result.getInt("b"), result.getFloat("c")));
                    }
                }
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        context.setVariable("data", data);

        String html = templateEngine.process("index", context);
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        try (OutputStream outputStream = new FileOutputStream("index.pdf")) {
            renderer.createPDF(outputStream);
        } catch (DocumentException | IOException e) {
            System.err.println("error");
        }
        System.out.println("PDF creation completed");
    }
}

@AllArgsConstructor
@Getter
class Data {
    private String a;
    private int b;
    private float c;
}