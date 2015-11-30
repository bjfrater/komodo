/*
* JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.komodo.rest.relational;

import java.net.URI;
import org.komodo.relational.model.Model;
import org.komodo.relational.vdb.ModelSource;
import org.komodo.relational.vdb.Vdb;
import org.komodo.rest.KomodoService;
import org.komodo.rest.RestBasicEntity;
import org.komodo.rest.RestLink;
import org.komodo.rest.RestLink.LinkType;
import org.komodo.spi.KException;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.ArgCheck;
import org.modeshape.sequencer.teiid.lexicon.VdbLexicon;

/**
 * A VDB model source that can be used by GSON to build a JSON model source representation.
 */
public final class RestVdbModelSource extends RestBasicEntity {

    /**
     * Label used to describe jndi name
     */
    public static final String JNDI_NAME_LABEL = KomodoService.encode(VdbLexicon.Source.JNDI_NAME);

    /**
     * Label used to describe translator
     */
    public static final String TRANSLATOR_LABEL = KomodoService.encode(VdbLexicon.Source.TRANSLATOR);

    /**
     * Constructor for use when deserializing
     */
    public RestVdbModelSource() {
        super();
    }

    /**
     * Constructor for use when serializing.
     * @param baseUri the base uri of the REST request
     * @param source the source
     * @param uow the transaction
     * @throws KException if error occurs
     */
    public RestVdbModelSource(URI baseUri, ModelSource source, UnitOfWork uow) throws KException {
        super(baseUri, source, uow);

        setJndiName(source.getJndiName(uow));
        setTranslator(source.getTranslatorName(uow));

        Model model = ancestor(source, Model.class, uow);
        ArgCheck.isNotNull(model);
        String modelName = model.getName(uow);

        Vdb vdb = ancestor(model, Vdb.class, uow);
        ArgCheck.isNotNull(vdb);
        String vdbName = vdb.getName(uow);

        addLink(new RestLink(LinkType.SELF, getUriBuilder().buildVdbModelSourceUri(LinkType.SELF, vdbName, modelName, getId())));
        addLink(new RestLink(LinkType.PARENT, getUriBuilder().buildVdbModelSourceUri(LinkType.PARENT, vdbName, modelName, getId())));
    }


    /**
     * @return the jndiName
     */
    public String getJndiName() {
        Object jndi = tuples.get(JNDI_NAME_LABEL);
        return jndi != null ? jndi.toString() : null;
    }

    /**
     * @param jndiName the jndiName to set
     */
    public void setJndiName(String jndiName) {
        tuples.put(JNDI_NAME_LABEL, jndiName);
    }

    /**
     * @return the translator
     */
    public String getTranslator() {
        Object translator = tuples.get(TRANSLATOR_LABEL);
        return translator != null ? translator.toString() : null;
    }

    /**
     * @param translator the translator to set
     */
    public void setTranslator(String translator) {
        tuples.put(TRANSLATOR_LABEL, translator);
    }
}