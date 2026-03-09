package io.mosip.testrig.dslrig.dataprovider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import io.mosip.testrig.dslrig.dataprovider.models.*;
import io.mosip.testrig.dslrig.dataprovider.preparation.MosipMasterData;
import io.mosip.testrig.dslrig.dataprovider.variables.VariableManager;

public class DocumentProvider {

    private static TemplateEngine initTemplateEngine(String contextKey) {

        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(System.getProperty("java.io.tmpdir")
                + VariableManager.getVariableValue(contextKey, "mosip.test.persona.documentsdatapath"));

        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        return engine;
    }

    private static String parseTemplate(String templateName,
                                        String photo,
                                        String name,
                                        String date,
                                        String address,
                                        TemplateEngine engine) {

        Context context = new Context();
        context.setVariable("myphoto", photo);
        context.setVariable("date", date);
        context.setVariable("name", name);
        context.setVariable("address", address);

        return engine.process(templateName, context);
    }

    private static void generatePdfFromHtml(String html, File outFile) {
        try (OutputStream outputStream =
                     new BufferedOutputStream(new FileOutputStream(outFile))) {

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void updateVariable(String contextKey,
                                       String variable,
                                       String newPath) {

        String existing = VariableManager.getVariableValue(contextKey, variable) != null
                ? VariableManager.getVariableValue(contextKey, variable).toString()
                : "";

        String updated = existing.isEmpty()
                ? newPath
                : existing + "," + newPath;

        VariableManager.setVariableValue(contextKey, variable, updated);
    }

    private static String buildAddress(ResidentModel res) {

        StringBuilder addr = new StringBuilder();

        Hashtable<String, MosipLocationModel> locs = res.getLocation();
        Set<String> keys = locs.keySet();

        for (String k : keys) {
            MosipLocationModel loc = locs.get(k);
            addr.append(" ").append(loc.getName());
        }

        return addr.toString();
    }

    private static LocalDate calculateLicenseDate(String dob) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        LocalDate birthDate = LocalDate.parse(dob, formatter);

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        return birthDate.plusYears(age + 5);
    }

    public static List<MosipIDSchema> getDocTypesFromSchema(String contextKey) {

        List<MosipIDSchema> docSchema = new ArrayList<>();

        Hashtable<Double, Properties> schema =
                MosipMasterData.getIDSchemaLatestVersion(contextKey);

        Double schemaVersion = schema.keySet().iterator().next();

        List<MosipIDSchema> lstSchema =
                (List<MosipIDSchema>) schema.get(schemaVersion).get("schemaList");

        for (MosipIDSchema item : lstSchema) {

            if (!item.getRequired() && !item.getInputRequired())
                continue;

            if ("documentType".equals(item.getType()))
                docSchema.add(item);
        }

        return docSchema;
    }

    private static MosipDocCategoryModel getDocCategoryByType(
            String docType,
            List<MosipDocCategoryModel> docCats) {

        for (MosipDocCategoryModel m : docCats) {
            if (m.getIsActive() && m.getCode().equals(docType))
                return m;
        }

        return null;
    }

    public static List<MosipDocument> generateDocuments(
            ResidentModel res,
            String contextKey) throws Exception {

        List<String> docs = new ArrayList<>();

        TemplateEngine templateEngine = initTemplateEngine(contextKey);

        String name = res.getName().getFirstName() + " " + res.getName().getSurName();
        String photo = res.getBiometric().getEncodedPhoto();

        String address = buildAddress(res);

        LocalDate licenseDate = calculateLicenseDate(res.getDob());

        String date = licenseDate.toString();

        /* Passport */

        String passportHtml =
                parseTemplate("passport", photo, name, date, address, templateEngine);

        File passportFile = File.createTempFile("Passport_", ".pdf");

        generatePdfFromHtml(passportHtml, passportFile);

        docs.add(passportFile.getAbsolutePath());

        updateVariable(contextKey, "Passport_", passportFile.getAbsolutePath());

        /* Driving License */

        String dlHtml =
                parseTemplate("driverlicense", photo, name, date, address, templateEngine);

        File dlFile = File.createTempFile("DrivingLic_", ".pdf");

        generatePdfFromHtml(dlHtml, dlFile);

        docs.add(dlFile.getAbsolutePath());

        updateVariable(contextKey, "DrivingLic_", dlFile.getAbsolutePath());

        /* Map documents */

        List<MosipDocument> lstDocs = new ArrayList<>();

        List<MosipDocCategoryModel> docCats =
                MosipMasterData.getDocumentCategories(contextKey);

        List<MosipIDSchema> schemaList =
                getDocTypesFromSchema(contextKey);

        for (MosipIDSchema schema : schemaList) {

            MosipDocCategoryModel catModel =
                    getDocCategoryByType(schema.getSubType(), docCats);

            if (catModel == null)
                continue;

            List<MosipDocTypeModel> allDocTypes =
                    MosipMasterData.getMappedDocumentTypes(
                            catModel.getCode(),
                            catModel.getLangCode(),
                            contextKey);

            if (allDocTypes == null || allDocTypes.isEmpty())
                continue;

            MosipDocument doc = new MosipDocument();

            doc.setDocCategoryName(catModel.getName());
            doc.setDocCategoryCode(catModel.getCode());
            doc.setDocCategoryLang(catModel.getLangCode());

            List<MosipDocTypeModel> docTypes = new ArrayList<>();
            List<String> catDocs = new ArrayList<>();

            doc.setType(docTypes);
            doc.setDocs(catDocs);

            int i = 0;

            for (MosipDocTypeModel dt : allDocTypes) {

                docTypes.add(dt);

                catDocs.add(docs.get(i % docs.size()));

                i++;
            }

            lstDocs.add(doc);
        }

        return lstDocs;
    }
}