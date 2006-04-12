/**
 *  Copyright (C) 2005 Orbeon, Inc.
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version
 *  2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.xforms.function;

import org.orbeon.saxon.expr.Expression;
import org.orbeon.saxon.expr.StaticContext;
import org.orbeon.saxon.expr.StaticProperty;
import org.orbeon.saxon.expr.XPathContext;
import org.orbeon.saxon.om.Item;
import org.orbeon.saxon.om.SingletonIterator;
import org.orbeon.saxon.value.IntegerValue;
import org.orbeon.saxon.trans.XPathException;

public class Last extends XFormsFunction {


    public Expression preEvaluate(StaticContext env) {
        return this;
    }

    public Item evaluateItem(XPathContext c) throws XPathException {
        if (c.getCurrentIterator() instanceof SingletonIterator) {
            // We have a top level expression and Saxon does not know about the context nodeset
            return new IntegerValue(getXFormsControls().getCurrentNodeset().size());
        } else {
            return new IntegerValue(c.getLast());
        }
    }

    public int getIntrinsicDependencies() {
        return StaticProperty.DEPENDS_ON_LAST;
    }
}
