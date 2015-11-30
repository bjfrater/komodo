/*
* JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.komodo.rest.relational.json;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.jboss.resteasy.util.Encode;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.vdb.Translator;
import org.komodo.relational.vdb.Vdb;
import org.komodo.rest.RestProperty;
import org.komodo.rest.relational.RestVdbTranslator;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyDescriptor;
import org.mockito.Mockito;

@SuppressWarnings( { "javadoc", "nls" } )
public final class VdbTranslatorSerializerTest extends AbstractSerializerTest {

    private static final String TR_DATA_PATH = VDB_DATA_PATH + "/vdbTranslators/MyTranslator";
    private static final String DESCRIPTION = "my description";
    private static final String NAME = "MyTranslator";
    private static final int NUM_PROPS = 3;
    private static final String TYPE = "oracle";
    private static final String[] PROPS_KEYS = {"larry", "magic", "michael"};
    private static final String[] PROPS_VALUES = {"bird", "johnson", "jordan"};
    private static final List<RestProperty> PROPS = new ArrayList<>();
    static {
        for (int i = 0; i < PROPS_KEYS.length; ++i) {
            PROPS.add(new RestProperty(PROPS_KEYS[i], PROPS_VALUES[i]));
        }
    }

    private static final String JSON = EMPTY_STRING +
    OPEN_BRACE + NEW_LINE +
    "  \"" + ID + "\": \"" + NAME + "\"," + NEW_LINE +
    "  \"" + BASE_URI + "\": \"" + MY_BASE_URI + "\"," + NEW_LINE +
    "  \"" + DATA_PATH + "\": \"" + TR_DATA_PATH + "\"," + NEW_LINE +
    "  \"" + KTYPE + "\": \"" + KomodoType.VDB_TRANSLATOR.getType() + "\"," + NEW_LINE +
    "  \"" + HAS_CHILDREN + "\": false," + NEW_LINE +
    "  \"vdb__description\": \"my description\"" + COMMA + NEW_LINE +
    "  \"vdb__type\": \"oracle\"" + COMMA + NEW_LINE +
    "  \"keng__properties\": " + OPEN_SQUARE_BRACKET + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"name\": \"larry\"" + COMMA + NEW_LINE +
        "      \"value\": \"bird\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
       "    " +  OPEN_BRACE + NEW_LINE +
        "      \"name\": \"magic\"" + COMMA + NEW_LINE +
        "      \"value\": \"johnson\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"name\": \"michael\"" + COMMA + NEW_LINE +
        "      \"value\": \"jordan\"" + NEW_LINE +
        "    " + CLOSE_BRACE + NEW_LINE +
        "  " + CLOSE_SQUARE_BRACKET + COMMA + NEW_LINE +
        "  \"" + LINKS + "\": " + OPEN_SQUARE_BRACKET + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"self\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbTranslators/MyTranslator\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"parent\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"children\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + SEARCH + "parent\\u003d" + Encode.encodeQueryParam(TR_DATA_PATH) + "\"" + NEW_LINE +
        "    " + CLOSE_BRACE + NEW_LINE +
        "  " + CLOSE_SQUARE_BRACKET + NEW_LINE +
    CLOSE_BRACE;

    private RestVdbTranslator translator;

    @Before
    public void init() throws Exception {
        Vdb theVdb = mockObject(Vdb.class, VDB_NAME, VDB_DATA_PATH, KomodoType.VDB, true);

        Translator theTranslator = mockObject(Translator.class,
                                                      NAME,
                                                      TR_DATA_PATH,
                                                      KomodoType.VDB_TRANSLATOR,
                                                      false);

        Mockito.when(theTranslator.getParent(transaction)).thenReturn(theVdb);
        Mockito.when(theTranslator.getPropertyNames(transaction)).thenReturn(PROPS_KEYS);
        Mockito.when(theTranslator.getPropertyDescriptors(transaction)).thenReturn(new PropertyDescriptor[0]);

        this.translator = new RestVdbTranslator(MY_BASE_URI, theTranslator, transaction);
        this.translator.setDescription( DESCRIPTION );
        this.translator.setType(TYPE);
        this.translator.setProperties( PROPS );
    }

    @Test
    public void shouldExportJson() {
        String json = KomodoJsonMarshaller.marshall( this.translator );
        assertEquals(JSON, json);
    }

    @Test
    public void shouldImportJson() {
        final RestVdbTranslator translator = KomodoJsonMarshaller.unmarshall( JSON, RestVdbTranslator.class );
        assertThat( translator.getDescription(), is( DESCRIPTION ) );
        assertThat( translator.getId(), is( NAME ) );
        assertThat( translator.getType(), is( TYPE ) );
        assertThat( translator.getLinks().size(), is( 3 ) );
        assertEquals( translator.getProperties().size(), NUM_PROPS);

        for (RestProperty property : translator.getProperties()) {
            assertTrue(PROPS.contains(property));
        }
    }

    @Test( expected = Exception.class )
    public void shouldNotExportWhenNameIsMissing() {
        final RestVdbTranslator incomplete = new RestVdbTranslator();
        translator.setType( TYPE );
        KomodoJsonMarshaller.marshall( incomplete );
    }

    @Test( expected = Exception.class )
    public void shouldNotExportWhenTypeIsMissing() {
        final RestVdbTranslator incomplete = new RestVdbTranslator();
        translator.setId( NAME );
        KomodoJsonMarshaller.marshall( incomplete );
    }

    @Test( expected = Exception.class )
    public void shouldNotImportJsonWhenIdIsMissing() {
        final String malformed = "{\"type\":\"oracle\",\"description\":\"my description\",\"properties\":{\"magic\":\"johnson\",\"michael\":\"jordan\",\"larry\":\"bird\"}}";
        KomodoJsonMarshaller.unmarshall( malformed, RestVdbTranslator.class );
    }

    @Test( expected = Exception.class )
    public void shouldNotImportJsonWhenTypeIsMissing() {
        final String malformed = "{\"id\":\"MyTranslator\",\"description\":\"my description\",\"properties\":{\"magic\":\"johnson\",\"michael\":\"jordan\",\"larry\":\"bird\"}}";
        KomodoJsonMarshaller.unmarshall( malformed, RestVdbTranslator.class );
    }

}