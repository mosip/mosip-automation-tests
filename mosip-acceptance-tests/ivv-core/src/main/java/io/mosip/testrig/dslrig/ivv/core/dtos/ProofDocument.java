package io.mosip.testrig.dslrig.ivv.core.dtos;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProofDocument {

    public enum DOCUMENT_CATEGORY {POA, POB, POI, POR, POEX, POEM};
    private DOCUMENT_CATEGORY docCatCode = DOCUMENT_CATEGORY.POI;
    private String docTypeCode = "";
    private String docFileFormat = "";
    private String docId = "";
    private String name = "";
    private String path = "";
    private ArrayList<String> tags = new ArrayList();
}
