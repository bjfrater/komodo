/*
 * JBoss, Home of Professional Open Source.
*
* See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
*
* See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
*/
package org.komodo.rest.relational;

import java.net.URI;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Permission;
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
 * A data role that can be used by GSON to build a JSON document representation.
 *
 * <pre>
 * <code>
 * {
 *     "id" : "MyPermission",
 *     "allowAlter" : true,
 *     "allowCreate" : true,
 *     "allowDelete" : true,
 *     "allowExecute" : true,
 *     "allowLanguage" : true,
 *     "allowRead" : true,
 *     "allowUpdate" : true,
 *     "conditions" : {
 *         "cant" : true,
 *         "buy" : false,
 *         "me" : true,
 *         "love" : false
 *     },
 *     "masks" : {
 *         "love" : "words",
 *         "me" : "of",
 *         "do" : "love"
 *     }
 * }
 * </code>
 * </pre>
 */
public final class RestVdbPermission extends RestBasicEntity {

    /**
     * Label used to describe name
     */
    public static final String NAME_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.PERMISSION);

    /**
     * Label used to describe allowAlter
     */
    public static final String ALLOW_ALTER_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_ALTER);

    /**
     * Label used to describe allowCreate
     */
    public static final String ALLOW_CREATE_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_CREATE);

    /**
     * Label used to describe allowDelete
     */
    public static final String ALLOW_DELETE_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_DELETE);

    /**
     * Label used to describe allowExecute
     */
    public static final String ALLOW_EXECUTE_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_EXECUTE);

    /**
     * Label used to describe allowLanguage
     */
    public static final String ALLOW_LANGUAGE_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_LANGUAGE);

    /**
     * Label used to describe allowRead
     */
    public static final String ALLOW_READ_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_READ);

    /**
     * Label used to describe allowUpdate
     */
    public static final String ALLOW_UPDATE_LABEL = KomodoService.encode(VdbLexicon.DataRole.Permission.ALLOW_UPDATE);

    /**
     * An empty array of permissions.
     */
    public static final RestVdbPermission[] NO_PERMISSIONS = new RestVdbPermission[ 0 ];

    /**
     * Constructor for use <strong>only</strong> when deserializing.
     */
    public RestVdbPermission() {
        setAllowAlter(Permission.DEFAULT_ALLOW_ALTER);
        setAllowCreate(Permission.DEFAULT_ALLOW_CREATE);
        setAllowDelete(Permission.DEFAULT_ALLOW_DELETE);
        setAllowExecute(Permission.DEFAULT_ALLOW_EXECUTE);
        setAllowLanguage(Permission.DEFAULT_ALLOW_LANGUAGE);
        setAllowRead(Permission.DEFAULT_ALLOW_READ);
        setAllowUpdate(Permission.DEFAULT_ALLOW_UPDATE);
    }

    /**
     * Constructor for use when serializing.
     * @param baseUri the base uri of the REST request
     * @param permission the permission
     * @param uow the transaction
     * @throws KException if error occurs
     */
    public RestVdbPermission(URI baseUri, Permission permission, UnitOfWork uow) throws KException {
        super(baseUri, permission, uow);

        setName(permission.getName(uow));
        setAllowAlter(permission.isAllowAlter(uow));
        setAllowCreate(permission.isAllowCreate(uow));
        setAllowDelete(permission.isAllowDelete(uow));
        setAllowExecute(permission.isAllowExecute(uow));
        setAllowLanguage(permission.isAllowLanguage(uow));
        setAllowRead(permission.isAllowRead(uow));
        setAllowUpdate(permission.isAllowUpdate(uow));

        DataRole dataRole = ancestor(permission, DataRole.class, uow);
        ArgCheck.isNotNull(dataRole);
        String dataRoleName = dataRole.getName(uow);

        Vdb vdb = ancestor(dataRole, Vdb.class, uow);
        ArgCheck.isNotNull(vdb);
        String vdbName = vdb.getName(uow);

        addLink(new RestLink(LinkType.SELF, getUriBuilder().buildVdbPermissionUri(LinkType.SELF, vdbName, dataRoleName, getId())));
        addLink(new RestLink(LinkType.PARENT, getUriBuilder().buildVdbPermissionUri(LinkType.PARENT, vdbName, dataRoleName, getId())));
        addLink(new RestLink(LinkType.CONDITIONS, getUriBuilder().buildVdbPermissionUri(LinkType.CONDITIONS, vdbName, dataRoleName, getId())));
        addLink(new RestLink(LinkType.MASKS, getUriBuilder().buildVdbPermissionUri(LinkType.MASKS, vdbName, dataRoleName, getId())));
    }

    /**
     * @return the name (can be empty)
     */
    public String getName() {
        Object name = tuples.get(NAME_LABEL);
        return name != null ? name.toString() : null;
    }
    /**
     * @return <code>true</code> if allows alter
     */
    public boolean isAllowAlter() {
        Object value = tuples.get(ALLOW_ALTER_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_ALTER;
    }

    /**
     * @return <code>true</code> if allows create
     */
    public boolean isAllowCreate() {
        Object value = tuples.get(ALLOW_CREATE_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_CREATE;
    }

    /**
     * @return <code>true</code> if allows delete
     */
    public boolean isAllowDelete() {
        Object value = tuples.get(ALLOW_DELETE_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_DELETE;
    }

    /**
     * @return <code>true</code> if allows execute
     */
    public boolean isAllowExecute() {
        Object value = tuples.get(ALLOW_EXECUTE_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_EXECUTE;
    }

    /**
     * @return <code>true</code> if allows language
     */
    public boolean isAllowLanguage() {
        Object value = tuples.get(ALLOW_LANGUAGE_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_LANGUAGE;
    }

    /**
     * @return <code>true</code> if allows read
     */
    public boolean isAllowRead() {
        Object value = tuples.get(ALLOW_READ_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_READ;
    }

    /**
     * @return <code>true</code> if allows update
     */
    public boolean isAllowUpdate() {
        Object value = tuples.get(ALLOW_UPDATE_LABEL);
        return value != null ? Boolean.parseBoolean(value.toString()) : Permission.DEFAULT_ALLOW_UPDATE;
    }

    /**
     * @param newAllowAlter
     *        <code>true</code> if allows alter
     */
    public void setAllowAlter( final boolean newAllowAlter ) {
        tuples.put(ALLOW_ALTER_LABEL, newAllowAlter);
    }

    /**
     * @param newAllowCreate
     *        <code>true</code> if allows create
     */
    public void setAllowCreate( final boolean newAllowCreate ) {
        tuples.put(ALLOW_CREATE_LABEL, newAllowCreate);
    }

    /**
     * @param newAllowDelete
     *        <code>true</code> if allows delete
     */
    public void setAllowDelete( final boolean newAllowDelete ) {
        tuples.put(ALLOW_DELETE_LABEL, newAllowDelete);
    }

    /**
     * @param newAllowExecute
     *        <code>true</code> if allows execute
     */
    public void setAllowExecute( final boolean newAllowExecute ) {
        tuples.put(ALLOW_EXECUTE_LABEL, newAllowExecute);
    }

    /**
     * @param newAllowLanguage
     *        <code>true</code> if allows language
     */
    public void setAllowLanguage( final boolean newAllowLanguage ) {
        tuples.put(ALLOW_LANGUAGE_LABEL, newAllowLanguage);
    }

    /**
     * @param newAllowRead
     *        <code>true</code> if allows read
     */
    public void setAllowRead( final boolean newAllowRead ) {
        tuples.put(ALLOW_READ_LABEL, newAllowRead);
    }

    /**
     * @param newAllowUpdate
     *        <code>true</code> if allows update
     */
    public void setAllowUpdate( final boolean newAllowUpdate ) {
        tuples.put(ALLOW_UPDATE_LABEL, newAllowUpdate);
    }

    /**
     * @param newName
     *        the new translator name (can be empty)
     */
    public void setName( final String newName ) {
        tuples.put(NAME_LABEL, newName);
    }
}