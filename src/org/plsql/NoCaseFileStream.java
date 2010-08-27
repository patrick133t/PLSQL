/* Copyright 2010 Patrick Higgins
 *
 * This file is part of PLSQL.
 *
 * PLSQL is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * PLSQL is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with PLSQL.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.plsql;

import java.io.IOException;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;

public class NoCaseFileStream extends ANTLRFileStream {

	public NoCaseFileStream(String fileName) throws IOException {
		super(fileName);
	}

	public NoCaseFileStream(String fileName, String encoding)
			throws IOException {
		super(fileName, encoding);
	}

    public int LA(int i) {
        if ( i==0 ) {
            return 0; // undefined
        }
        if ( i<0 ) {
            i++; // e.g., translate LA(-1) to use offset 0
        }

        if ( (p+i-1) >= n ) {

            return CharStream.EOF;
        }
        return Character.toLowerCase(data[p+i-1]);
    }
	
}
