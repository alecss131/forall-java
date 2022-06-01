package ru.example;

import com.lowagie.text.DocumentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.*;
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

        Configuration configuration = new Configuration().addAnnotatedClass(Data.class)
                .setProperty(Environment.DRIVER, "org.postgresql.Driver")
                .setProperty(Environment.URL, "jdbc:postgresql://127.0.0.1:5432/testdb")
                .setProperty(Environment.USER, "user")
                .setProperty(Environment.PASS, "user")
                .setProperty(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty(Environment.SHOW_SQL, "true")
                .setProperty(Environment.HBM2DDL_AUTO, "update");
        try (SessionFactory factory = configuration.buildSessionFactory();
             Session session = factory.openSession()) {
            List<Data> data = session.createQuery("select a from Data a", Data.class).getResultList();
            if (data.size() == 0) {
                Transaction tx = session.beginTransaction();
                Random rnd = new Random();
                session.persist(new Data("data", rnd.nextInt(), rnd.nextFloat()));
                session.persist(new Data("another data", rnd.nextInt(), rnd.nextFloat()));
                session.persist(new Data("test", rnd.nextInt(), rnd.nextFloat()));
                session.persist(new Data("some data", rnd.nextInt(), rnd.nextFloat()));
                session.persist(new Data("string", rnd.nextInt(), rnd.nextFloat()));
                tx.commit();
                data = session.createQuery("select a from Data a", Data.class).getResultList();
            }
            context.setVariable("data", data);
        }

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

@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "data")
class Data {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String a;
    private int b;
    private float c;
    public Data(String a, int b, float c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}