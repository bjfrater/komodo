/*
* JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.komodo.rest.relational;

import java.net.URI;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.vdb.VdbImport;
import org.komodo.rest.KomodoService;
import org.komodo.rest.RestBasicEntity;
import org.komodo.rest.RestLink;
import org.komodo.rest.RestLink.LinkType;
import org.komodo.spi.KException;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.modeshape.sequencer.teiid.lexicon.VdbLexicon;
import org.teiid.core.util.ArgCheck;

/**
 * A VDB import that can be used by GSON to build a JSON document representation.
 *
 * <pre>
 * <code>
 * {
 *     "id" : "MyImportVdb",
 *     "version" : "1",
 *     "importDataPolicies" : "true"
 * }
 * </code>
 * </pre>
 */
public final class RestVdbImport extends RestBasicEntity {

    /**
     * Label used to describe name
     */
    public static final String NAME_LABEL = KomodoService.encode(VdbLexicon.ImportVdb.IMPORT_VDB);

    /**
     * Label used to describe import data policies
     */
    public static final String IMPORT_POLICIES_LABEL = KomodoService.encode(VdbLexicon.ImportVdb.IMPORT_DATA_POLICIES);

    /**
     * Label used to describe version
     */
    public static final String VERSION_LABEL = KomodoService.encode(VdbLexicon.ImportVdb.VERSION);

    /**
     * And empty array of VDB imports.
     */
    public static final RestVdbImport[] NO_IMPORTS = new RestVdbImport[ 0 ];

    /**
     * Constructor for use <strong>only</strong> when deserializing.
     */
    public RestVdbImport() {
        setVersion(Vdb.DEFAULT_VERSION);
        setImportDataPolicies(VdbImport.DEFAULT_IMPORT_DATA_POLICIES);
    }

    /**
     * Constructor for use when serializing.
     * @param baseUri the base uri of the REST request
     * @param vdbImport the import
     * @param uow the transaction
     * @throws KException if error occurs
     */
    public RestVdbImport(URI baseUri, VdbImport vdbImport, UnitOfWork uow) throws KException {
        super(baseUri, vdbImport, uow);

        setName(vdbImport.getName(uow));
        setVersion(vdbImport.getVersion(uow));
        setImportDataPolicies(vdbImport.isImportDataPolicies(uow));

        Vdb vdb = ancestor(vdbImport, Vdb.class, uow);
        ArgCheck.isNotNull(vdb);
        String vdbName = vdb.getName(uow);

        addLink(new RestLink(LinkType.SELF, getUriBuilder().buildVdbImportUri(LinkType.SELF, vdbName, getId())));
        addLink(new RestLink(LinkType.PARENT, getUriBuilder().buildVdbImportUri(LinkType.PARENT, vdbName, getId())));
    }

    /**
     * @return the name (can be empty)
     */
    public String getName() {
        Object value = tuples.get(NAME_LABEL);
        return value != null ? value.toString() : null;
    }

    /**
     * @param newName
     *        the new VDB import name (can be empty)
     */
    public void setName( final String newName ) {
        tuples.put(NAME_LABEL, newName);
    }

    /**
     * @return the VDB version
     */
    public int getVersion() {
        Object value = tuples.get(VERSION_LABEL);
        return value != null ? Integer.parseInt(value.toString()) : Vdb.DEFAULT_VERSION;
    }

    /**
     * @param newVersion
     *        the new VDB import version
     */
    public void setVersion( final int newVersion ) {
        tuples.put(VERSION_LABEL, newVersion);
    }

    /**
     * @return <code>true</code> if importing data policies
     */
    public boolean isImportDataPolicies() {
        Object value = tuples.get(IMPORT_POLICIES_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : VdbImport.DEFAULT_IMPORT_DATA_POLICIES;
    }

    /**
     * @param newImportDataPolicies
     *        the new import data policies
     */
    public void setImportDataPolicies( final boolean newImportDataPolicies ) {
        tuples.put(IMPORT_POLICIES_LABEL, newImportDataPolicies);
    }
}