/*
 * JBoss, Home of Professional Open Source.
 *
 * See the LEGAL.txt file distributed with this work for information regarding copyright ownership and licensing.
 *
 * See the AUTHORS.txt file distributed with this work for a full listing of individual contributors.
 */
package org.komodo.relational.model.internal;

import java.util.ArrayList;
import java.util.List;
import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.RelationalProperties;
import org.komodo.relational.internal.AdapterFactory;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.internal.TypeResolver;
import org.komodo.relational.model.AccessPattern;
import org.komodo.relational.model.Column;
import org.komodo.relational.model.ForeignKey;
import org.komodo.relational.model.Index;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.PrimaryKey;
import org.komodo.relational.model.StatementOption;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.UniqueConstraint;
import org.komodo.repository.ObjectImpl;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.StringUtils;
import org.modeshape.jcr.JcrLexicon;
import org.modeshape.sequencer.ddl.StandardDdlLexicon;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.Constraint;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.CreateTable;
import org.modeshape.sequencer.ddl.dialect.teiid.TeiidDdlLexicon.SchemaElement;

/**
 * An implementation of a relational model table.
 */
public class TableImpl extends RelationalObjectImpl implements Table {

    private enum StandardOptions {

        ANNOTATION,
        CARDINALITY,
        MATERIALIZED,
        MATERIALIZED_TABLE,
        NAMEINSOURCE,
        UPDATABLE,
        UUID

    }

    /*

      - ddl:temporary (STRING) < 'GLOBAL', 'LOCAL'
      - ddl:onCommitValue (STRING) < 'DELETE ROWS', 'PRESERVE ROWS'
      - teiidddl:schemaElementType (string) = 'FOREIGN' mandatory autocreated < 'FOREIGN', 'VIRTUAL'
      - teiidddl:queryExpression (string)
    + * (ddl:columnDefinition) = ddl:columnDefinition sns
    // TODO + * (ddl:tableConstraint) = ddl:tableConstraint sns
    + * (ddl:statementOption) = ddl:statementOption sns
    + * (teiidddl:constraint) = teiidddl:constraint sns

     */

    /**
     * The resolver of a {@link Table}.
     */
    public static final TypeResolver< Table > RESOLVER = new TypeResolver< Table >() {

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#create(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.Repository, org.komodo.spi.repository.KomodoObject, java.lang.String,
         *      org.komodo.relational.RelationalProperties)
         */
        @Override
        public Table create( final UnitOfWork transaction,
                             final Repository repository,
                             final KomodoObject parent,
                             final String id,
                             final RelationalProperties properties ) throws KException {
            final AdapterFactory adapter = new AdapterFactory( repository );
            final Model parentModel = adapter.adapt( transaction, parent, Model.class );
            return RelationalModelFactory.createTable( transaction, repository, parentModel, id );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#identifier()
         */
        @Override
        public KomodoType identifier() {
            return IDENTIFIER;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#owningClass()
         */
        @Override
        public Class< TableImpl > owningClass() {
            return TableImpl.class;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#resolvable(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public boolean resolvable( final UnitOfWork transaction,
                                   final KomodoObject kobject ) throws KException {
            return ObjectImpl.validateType( transaction, kobject.getRepository(), kobject, CreateTable.TABLE_STATEMENT );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.internal.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public Table resolve( final UnitOfWork transaction,
                              final KomodoObject kobject ) throws KException {
            return new TableImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }

    };

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param workspacePath
     *        the workspace relative path (cannot be empty)
     * @throws KException
     *         if an error occurs or if node at specified path is not a table
     */
    public TableImpl( final UnitOfWork uow,
                      final Repository repository,
                      final String workspacePath ) throws KException {
        super(uow, repository, workspacePath);
    }

    @Override
    public KomodoType getTypeIdentifier(UnitOfWork uow) {
        return RESOLVER.identifier();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#addAccessPattern(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public AccessPattern addAccessPattern( final UnitOfWork transaction,
                                           final String accessPatternName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( accessPatternName, "accessPatternName" ); //$NON-NLS-1$

        final AccessPattern result = RelationalModelFactory.createAccessPattern( transaction,
                                                                                 getRepository(),
                                                                                 this,
                                                                                 accessPatternName );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#addColumn(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public Column addColumn( final UnitOfWork transaction,
                             final String columnName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( columnName, "columnName" ); //$NON-NLS-1$

        final Column result = RelationalModelFactory.createColumn( transaction, getRepository(), this, columnName );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#addForeignKey(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String,
     *      org.komodo.relational.model.Table)
     */
    @Override
    public ForeignKey addForeignKey( final UnitOfWork transaction,
                                     final String foreignKeyName,
                                     final Table referencedTable ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( foreignKeyName, "foreignKeyName" ); //$NON-NLS-1$
        ArgCheck.isNotNull( referencedTable, "referencedTable" ); //$NON-NLS-1$

        final KomodoObject child = addChild( transaction, foreignKeyName, null );
        child.addDescriptor( transaction, Constraint.FOREIGN_KEY_CONSTRAINT );

        final String tableId = referencedTable.getRawProperty( transaction, JcrLexicon.UUID.getString() ).getStringValue( transaction );
        child.setProperty( transaction, Constraint.TABLE_REFERENCE, tableId );

        final ForeignKey result = new ForeignKeyImpl( transaction, getRepository(), child.getAbsolutePath() );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#addIndex(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public Index addIndex( final UnitOfWork transaction,
                           final String indexName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( indexName, "indexName" ); //$NON-NLS-1$

        final Index result = RelationalModelFactory.createIndex( transaction, getRepository(), this, indexName );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#addUniqueConstraint(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public UniqueConstraint addUniqueConstraint( final UnitOfWork transaction,
                                                 final String constraintName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( constraintName, "constraintName" ); //$NON-NLS-1$

        final UniqueConstraint result = RelationalModelFactory.createUniqueConstraint( transaction,
                                                                                       getRepository(),
                                                                                       this,
                                                                                       constraintName );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getAccessPatterns(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public AccessPattern[] getAccessPatterns( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< AccessPattern > result = new ArrayList< AccessPattern >();

        for ( final KomodoObject kobject : getChildrenOfType( transaction, Constraint.TABLE_ELEMENT ) ) {
            final Property prop = kobject.getProperty( transaction, Constraint.TYPE );

            if ( AccessPattern.CONSTRAINT_TYPE.toValue().equals( prop.getStringValue( transaction ) ) ) {
                final AccessPattern constraint = new AccessPatternImpl( transaction, getRepository(), kobject.getAbsolutePath() );
                result.add( constraint );
            }
        }

        if ( result.isEmpty() ) {
            return AccessPattern.NO_ACCESS_PATTERNS;
        }

        return result.toArray( new AccessPattern[ result.size() ] );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getCardinality(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public int getCardinality( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption(transaction, this, StandardOptions.CARDINALITY.name());

        if (option == null) {
            return Table.DEFAULT_CARDINALITY;
        }

        return Integer.parseInt(option.getOption(transaction));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChildren(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public KomodoObject[] getChildren( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final KomodoObject[] constraints = getChildrenOfType( transaction, Constraint.TABLE_ELEMENT ); // access patterns, primary key, unique constraints
        final Column[] columns = getColumns( transaction );
        final ForeignKey[] foreignKeys = getForeignKeys( transaction );
        final Index[] indexes = getIndexes( transaction );

        final int size = constraints.length + columns.length + foreignKeys.length + indexes.length;
        final KomodoObject[] result = new KomodoObject[ size ];
        System.arraycopy( constraints, 0, result, 0, constraints.length );
        System.arraycopy( columns, 0, result, constraints.length, columns.length );
        System.arraycopy( foreignKeys, 0, result, constraints.length + columns.length, foreignKeys.length );
        System.arraycopy( indexes, 0, result, constraints.length + columns.length + foreignKeys.length, indexes.length );

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getColumns(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Column[] getColumns( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< Column > result = new ArrayList< Column >();

        for ( final KomodoObject kobject : getChildrenOfType( transaction, CreateTable.TABLE_ELEMENT ) ) {
            final Column column = new ColumnImpl( transaction, getRepository(), kobject.getAbsolutePath() );
            result.add( column );
        }

        if ( result.isEmpty() ) {
            return Column.NO_COLUMNS;
        }

        return result.toArray( new Column[ result.size() ] );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.OptionContainer#getCustomOptions(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public StatementOption[] getCustomOptions( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        StatementOption[] result = getStatementOptions( transaction );

        if ( result.length != 0 ) {
            final List< StatementOption > temp = new ArrayList<>( result.length );

            for ( final StatementOption option : result ) {
                if ( StandardOptions.valueOf( option.getName( transaction ) ) == null ) {
                    temp.add( option );
                }
            }

            result = temp.toArray( new StatementOption[ temp.size() ] );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getDescription(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDescription( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption(transaction, this, StandardOptions.ANNOTATION.name());

        if (option == null) {
            return null;
        }

        return option.getOption(transaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getForeignKeys(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public ForeignKey[] getForeignKeys( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< ForeignKey > result = new ArrayList< ForeignKey >();

        for ( final KomodoObject kobject : getChildrenOfType( transaction, Constraint.FOREIGN_KEY_CONSTRAINT ) ) {
            final ForeignKey constraint = new ForeignKeyImpl( transaction, getRepository(), kobject.getAbsolutePath() );
            result.add( constraint );
        }

        if ( result.isEmpty() ) {
            return ForeignKey.NO_FOREIGN_KEYS;
        }

        return result.toArray( new ForeignKey[ result.size() ] );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getIndexes(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Index[] getIndexes( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< Index > result = new ArrayList< Index >();

        for ( final KomodoObject kobject : getChildrenOfType( transaction, Constraint.INDEX_CONSTRAINT ) ) {
            final Index constraint = new IndexImpl( transaction, getRepository(), kobject.getAbsolutePath() );
            result.add( constraint );
        }

        if ( result.isEmpty() ) {
            return Index.NO_INDEXES;
        }

        return result.toArray( new Index[ result.size() ] );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getMaterializedTable(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getMaterializedTable( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption(transaction, this, StandardOptions.MATERIALIZED_TABLE.name());

        if (option == null) {
            return null;
        }

        return option.getOption(transaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getNameInSource(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getNameInSource( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption(transaction, this, StandardOptions.NAMEINSOURCE.name());

        if (option == null) {
            return null;
        }

        return option.getOption(transaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getOnCommitValue(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public OnCommit getOnCommitValue( final UnitOfWork uow ) throws KException {
        final String value = getObjectProperty(uow, PropertyValueType.STRING, "getOnCommitValue", //$NON-NLS-1$
                                               StandardDdlLexicon.ON_COMMIT_VALUE);

        if (StringUtils.isBlank(value)) {
            return null;
        }

        return OnCommit.fromValue(value);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getPrimaryKey(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public PrimaryKey getPrimaryKey( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        PrimaryKey result = null;

        for ( final KomodoObject kobject : getChildrenOfType( transaction, Constraint.TABLE_ELEMENT ) ) {
            final Property prop = kobject.getProperty( transaction, Constraint.TYPE );

            if ( PrimaryKey.CONSTRAINT_TYPE.toValue().equals( prop.getStringValue( transaction ) ) ) {
                result = new PrimaryKeyImpl( transaction, getRepository(), kobject.getAbsolutePath() );
                break;
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getQueryExpression(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getQueryExpression( final UnitOfWork uow ) throws KException {
        return getObjectProperty(uow, PropertyValueType.STRING, "getQueryExpression", CreateTable.QUERY_EXPRESSION); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.SchemaElement#getSchemaElementType(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public SchemaElementType getSchemaElementType( final UnitOfWork uow ) throws KException {
        final String value = getObjectProperty(uow, PropertyValueType.STRING, "getSchemaElementType", //$NON-NLS-1$
                                               SchemaElement.TYPE);

        if (StringUtils.isBlank(value)) {
            return null;
        }

        return SchemaElementType.fromValue(value);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.OptionContainer#getStatementOptions(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public StatementOption[] getStatementOptions( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< StatementOption > result = new ArrayList< StatementOption >();

        for ( final KomodoObject kobject : getChildrenOfType( transaction, StandardDdlLexicon.TYPE_STATEMENT_OPTION ) ) {
            final StatementOption option = new StatementOptionImpl( transaction, getRepository(), kobject.getAbsolutePath() );

            if ( LOGGER.isDebugEnabled() ) {
                LOGGER.debug( "getStatementOptions: transaction = {0}, found statement option = {1}", //$NON-NLS-1$
                              transaction.getName(),
                              kobject.getAbsolutePath() );
            }

            result.add( option );
        }

        if ( result.isEmpty() ) {
            return StatementOption.NO_OPTIONS;
        }

        return result.toArray( new StatementOption[ result.size() ] );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getTemporaryTableType(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public TemporaryType getTemporaryTableType( final UnitOfWork uow ) throws KException {
        final String value = getObjectProperty(uow, PropertyValueType.STRING, "getTemporaryTableType", //$NON-NLS-1$
                                               StandardDdlLexicon.TEMPORARY);

        if (StringUtils.isBlank(value)) {
            return null;
        }

        return TemporaryType.fromValue(value);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KomodoObject#getTypeId()
     */
    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getUniqueConstraints(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public UniqueConstraint[] getUniqueConstraints( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final List< UniqueConstraint > result = new ArrayList< UniqueConstraint >();

        for ( final KomodoObject kobject : getChildrenOfType( transaction, Constraint.TABLE_ELEMENT ) ) {
            final Property prop = kobject.getProperty( transaction, Constraint.TYPE );

            if ( UniqueConstraint.CONSTRAINT_TYPE.toValue().equals( prop.getStringValue( transaction ) ) ) {
                final UniqueConstraint constraint = new UniqueConstraintImpl( transaction,
                                                                              getRepository(),
                                                                              kobject.getAbsolutePath() );
                result.add( constraint );
            }
        }

        if ( result.isEmpty() ) {
            return UniqueConstraint.NO_UNIQUE_CONSTRAINTS;
        }

        return result.toArray( new UniqueConstraint[ result.size() ] );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#getUuid(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getUuid( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption( transaction, this, StandardOptions.UUID.name() );

        if (option == null) {
            return null;
        }

        return option.getOption( transaction );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#isMaterialized(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public boolean isMaterialized( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption(transaction, this, StandardOptions.MATERIALIZED.name());

        if (option == null) {
            return Table.DEFAULT_MATERIALIZED;
        }

        return Boolean.parseBoolean(option.getOption(transaction));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#isUpdatable(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public boolean isUpdatable( final UnitOfWork transaction ) throws KException {
        final StatementOption option = Utils.getOption(transaction, this, StandardOptions.UPDATABLE.name());

        if (option == null) {
            return Table.DEFAULT_UPDATABLE;
        }

        return Boolean.parseBoolean(option.getOption(transaction));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#removeAccessPattern(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void removeAccessPattern( final UnitOfWork transaction,
                                     final String accessPatternToRemove ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( accessPatternToRemove, "accessPatternToRemove" ); //$NON-NLS-1$

        boolean found = false;
        final AccessPattern[] accessPatterns = getAccessPatterns( transaction );

        if ( accessPatterns.length != 0 ) {
            for ( final AccessPattern accessPattern : accessPatterns ) {
                if ( accessPatternToRemove.equals( accessPattern.getName( transaction ) ) ) {
                    accessPattern.remove( transaction );
                    found = true;
                    break;
                }
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.CONSTRAINT_NOT_FOUND_TO_REMOVE,
                                                      accessPatternToRemove,
                                                      AccessPattern.CONSTRAINT_TYPE.toString() ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#removeColumn(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeColumn( final UnitOfWork transaction,
                              final String columnToRemove ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( columnToRemove, "columnToRemove" ); //$NON-NLS-1$

        boolean found = false;
        final Column[] columns = getColumns( transaction );

        if ( columns.length != 0 ) {
            for ( final Column column : columns ) {
                if ( columnToRemove.equals( column.getName( transaction ) ) ) {
                    column.remove( transaction );
                    found = true;
                    break;
                }
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.COLUMN_NOT_FOUND_TO_REMOVE, columnToRemove ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#removeForeignKey(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeForeignKey( final UnitOfWork transaction,
                                  final String foreignKeyToRemove ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( foreignKeyToRemove, "foreignKeyToRemove" ); //$NON-NLS-1$

        boolean found = false;
        final ForeignKey[] foreignKeys = getForeignKeys( transaction );

        if ( foreignKeys.length != 0 ) {
            for ( final ForeignKey foreignKey : foreignKeys ) {
                if ( foreignKeyToRemove.equals( foreignKey.getName( transaction ) ) ) {
                    foreignKey.remove( transaction );
                    found = true;
                    break;
                }
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.CONSTRAINT_NOT_FOUND_TO_REMOVE,
                                                      foreignKeyToRemove,
                                                      ForeignKey.CONSTRAINT_TYPE.toString() ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#removeIndex(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void removeIndex( final UnitOfWork transaction,
                             final String indexToRemove ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( indexToRemove, "indexToRemove" ); //$NON-NLS-1$

        boolean found = false;
        final Index[] indexes = getIndexes( transaction );

        if ( indexes.length != 0 ) {
            for ( final Index index : indexes ) {
                if ( indexToRemove.equals( index.getName( transaction ) ) ) {
                    index.remove( transaction );
                    found = true;
                    break;
                }
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.CONSTRAINT_NOT_FOUND_TO_REMOVE,
                                                      indexToRemove,
                                                      Index.CONSTRAINT_TYPE.toString() ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#removePrimaryKey(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public void removePrimaryKey( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final PrimaryKey primaryKey = getPrimaryKey( transaction );

        if ( primaryKey == null ) {
            throw new KException( Messages.getString( Relational.CONSTRAINT_NOT_FOUND_TO_REMOVE,
                                                      PrimaryKey.CONSTRAINT_TYPE.toString(),
                                                      PrimaryKey.CONSTRAINT_TYPE.toString() ) );
        }

        primaryKey.remove( transaction );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.OptionContainer#removeStatementOption(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void removeStatementOption( final UnitOfWork transaction,
                                       final String optionToRemove ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( optionToRemove, "optionToRemove" ); //$NON-NLS-1$

        boolean found = false;
        final StatementOption[] options = getStatementOptions( transaction );

        if ( options.length != 0 ) {
            for ( final StatementOption option : options ) {
                if ( optionToRemove.equals( option.getName( transaction ) ) ) {
                    option.remove( transaction );
                    found = true;
                    break;
                }
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.STATEMENT_OPTION_NOT_FOUND_TO_REMOVE, optionToRemove ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#removeUniqueConstraint(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void removeUniqueConstraint( final UnitOfWork transaction,
                                        final String constraintToRemove ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( constraintToRemove, "constraintToRemove" ); //$NON-NLS-1$

        boolean found = false;
        final UniqueConstraint[] uniqueConstraints = getUniqueConstraints( transaction );

        if ( uniqueConstraints.length != 0 ) {
            for ( final UniqueConstraint uniqueConstraint : uniqueConstraints ) {
                if ( constraintToRemove.equals( uniqueConstraint.getName( transaction ) ) ) {
                    uniqueConstraint.remove( transaction );
                    found = true;
                    break;
                }
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.CONSTRAINT_NOT_FOUND_TO_REMOVE,
                                                      constraintToRemove,
                                                      UniqueConstraint.CONSTRAINT_TYPE.toString() ) );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setCardinality(org.komodo.spi.repository.Repository.UnitOfWork, int)
     */
    @Override
    public void setCardinality( final UnitOfWork transaction,
                                final int newCardinality ) throws KException {
        setStatementOption(transaction, StandardOptions.CARDINALITY.name(), Integer.toString(newCardinality));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setDescription(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setDescription( final UnitOfWork transaction,
                                final String newDescription ) throws KException {
        setStatementOption(transaction, StandardOptions.ANNOTATION.name(), newDescription);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setMaterialized(org.komodo.spi.repository.Repository.UnitOfWork, boolean)
     */
    @Override
    public void setMaterialized( final UnitOfWork transaction,
                                 final boolean newMaterialized ) throws KException {
        setStatementOption(transaction, StandardOptions.MATERIALIZED.name(), Boolean.toString(newMaterialized));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setMaterializedTable(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void setMaterializedTable( final UnitOfWork transaction,
                                      final String newMaterializedTable ) throws KException {
        setStatementOption(transaction, StandardOptions.MATERIALIZED_TABLE.name(), newMaterializedTable);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setNameInSource(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setNameInSource( final UnitOfWork transaction,
                                 final String newNameInSource ) throws KException {
        setStatementOption(transaction, StandardOptions.NAMEINSOURCE.name(), newNameInSource);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setOnCommitValue(org.komodo.spi.repository.Repository.UnitOfWork,
     *      org.komodo.relational.model.Table.OnCommit)
     */
    @Override
    public void setOnCommitValue( final UnitOfWork uow,
                                  final OnCommit newOnCommit ) throws KException {
        final String newValue = (newOnCommit == null) ? null : newOnCommit.toValue();
        setObjectProperty(uow, "setOnCommitValue", StandardDdlLexicon.ON_COMMIT_VALUE, newValue); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setPrimaryKey(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public PrimaryKey setPrimaryKey( final UnitOfWork transaction,
                                     final String newPrimaryKeyName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( newPrimaryKeyName, "newPrimaryKeyName" ); //$NON-NLS-1$

        // delete existing primary key (don't call removePrimaryKey as it throws exception if one does not exist)
        final PrimaryKey primaryKey = getPrimaryKey( transaction );

        if ( primaryKey != null ) {
            primaryKey.remove( transaction );
        }

        final PrimaryKey result = RelationalModelFactory.createPrimaryKey( transaction, getRepository(), this, newPrimaryKeyName );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setQueryExpression(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void setQueryExpression( final UnitOfWork uow,
                                    final String newQueryExpression ) throws KException {
        setObjectProperty(uow, "setQueryExpression", CreateTable.QUERY_EXPRESSION, newQueryExpression); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.SchemaElement#setSchemaElementType(org.komodo.spi.repository.Repository.UnitOfWork,
     *      org.komodo.relational.model.SchemaElement.SchemaElementType)
     */
    @Override
    public void setSchemaElementType( final UnitOfWork uow,
                                      final SchemaElementType newSchemaElementType ) throws KException {
        final String newValue = ((newSchemaElementType == null) ? SchemaElementType.DEFAULT_VALUE.name() : newSchemaElementType.name());
        setObjectProperty(uow, "setSchemaElementType", SchemaElement.TYPE, newValue); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.OptionContainer#setStatementOption(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public StatementOption setStatementOption( final UnitOfWork transaction,
                                               final String optionName,
                                               final String optionValue ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( optionName, "optionName" ); //$NON-NLS-1$

        StatementOption result = null;

        if ( StringUtils.isBlank( optionValue ) ) {
            removeStatementOption( transaction, optionName );
        } else {
            result = Utils.getOption( transaction, this, optionName );

            if ( result == null ) {
                result = RelationalModelFactory.createStatementOption( transaction,
                                                                       getRepository(),
                                                                       this,
                                                                       optionName,
                                                                       optionValue );
            } else {
                result.setOption( transaction, optionValue );
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setTemporaryTableType(org.komodo.spi.repository.Repository.UnitOfWork,
     *      org.komodo.relational.model.Table.TemporaryType)
     */
    @Override
    public void setTemporaryTableType( final UnitOfWork uow,
                                       final TemporaryType newTempType ) throws KException {
        final String newValue = ((newTempType == null) ? null : newTempType.name());
        setObjectProperty(uow, "setTemporaryTableType", StandardDdlLexicon.TEMPORARY, newValue); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setUpdatable(org.komodo.spi.repository.Repository.UnitOfWork, boolean)
     */
    @Override
    public void setUpdatable( final UnitOfWork transaction,
                              final boolean newUpdatable ) throws KException {
        setStatementOption(transaction, StandardOptions.UPDATABLE.name(), Boolean.toString(newUpdatable));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.Table#setUuid(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setUuid( final UnitOfWork transaction,
                         final String newUuid ) throws KException {
        setStatementOption( transaction, StandardOptions.UUID.name(), newUuid );
    }

}
