package org.eclipse.wst.jsdt.internal.ui.text.javadoc;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.internal.core.MetadataFile;
import org.eclipse.wst.jsdt.internal.oaametadata.DocumentedElement;

public class OAADocReader extends Reader {

	
	
	public OAADocReader(MetadataFile openable, IMember member) {

		getDoc(openable,member);
	}

	private void getDoc(MetadataFile openable, IMember member) {
		DocumentedElement documentation = openable.getDocumentation(member);
		if (documentation!=null)
		{
		 
		}
	}

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public int read(char[] arg0, int arg1, int arg2) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
