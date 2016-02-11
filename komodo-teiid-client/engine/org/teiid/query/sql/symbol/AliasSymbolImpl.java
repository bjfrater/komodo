/* Generated By:JJTree: Do not edit this line. AliasSymbol.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.teiid.query.sql.symbol;

import org.komodo.spi.query.sql.symbol.AliasSymbol;
import org.teiid.core.util.ArgCheck;
import org.teiid.query.parser.TCLanguageVisitorImpl;
import org.teiid.query.parser.TeiidClientParser;
import org.teiid.query.sql.lang.SingleElementSymbol;
import org.teiid.runtime.client.Messages;

/**
 * An AliasSymbol wraps a SingleElementSymbol and changes it's name.  AliasSymbols
 * should be used to perform the aliasing of elements in a SELECT clause.  They
 * should typically NOT be used elsewhere in a query.  The alias symbol takes on
 * the type of it's underlying SingleElementSymbol.  AliasSymbols are typically
 * applied to ElementSymbol, ExpressionSymbol, and AggregateSymbol.
 */
@SuppressWarnings( "unused" )
public class AliasSymbolImpl extends SymbolImpl
    implements SingleElementSymbol, BaseExpression, AliasSymbol<BaseExpression, TCLanguageVisitorImpl> {

    private BaseExpression symbol;

    /**
     * @param p
     * @param id
     */
    public AliasSymbolImpl(TeiidClientParser p, int id) {
        super(p, id);
    }

    /**
     * Get the type of the symbol
     * @return Type of the symbol
     */
    @Override
    public Class<?> getType() {
        return this.getSymbol().getType();
    }

    /**
     * @return the symbol
     */
    @Override
    public BaseExpression getSymbol() {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    @Override
    public void setSymbol(BaseExpression symbol) {
		ArgCheck.isTrue(! (symbol instanceof AliasSymbolImpl), Messages.getString(Messages.ERR.ERR_015_010_0029));
		ArgCheck.isNotNull(symbol, Messages.getString(Messages.ERR.ERR_015_010_0029));
        this.symbol = symbol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.symbol == null) ? 0 : this.symbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        AliasSymbolImpl other = (AliasSymbolImpl)obj;
        if (this.symbol == null) {
            if (other.symbol != null) return false;
        } else if (!this.symbol.equals(other.symbol)) return false;
        return true;
    }

    /** Accept the visitor. **/
    @Override
    public void acceptVisitor(TCLanguageVisitorImpl visitor) {
        visitor.visit(this);
    }

    @Override
    public AliasSymbolImpl clone() {
        AliasSymbolImpl clone = new AliasSymbolImpl(this.parser, this.id);

        if(getSymbol() != null)
            clone.setSymbol(getSymbol().clone());
        if(getShortCanonicalName() != null)
            clone.setShortCanonicalName(getShortCanonicalName());
        if(outputName != null)
            clone.outputName = outputName;
        if(getShortName() != null)
            clone.setShortName(getShortName());
        if(getName() != null)
            clone.setName(getName());

        return clone;
    }

}
/* JavaCC - OriginalChecksum=462ff7272cb5be687c156dcb9525f2f7 (do not edit this line) */