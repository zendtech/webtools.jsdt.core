package org.eclipse.wst.jsdt.internal.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.jsdt.internal.ui.navigator.ContainerFolder;

public class ContainerFolderAdapterFactory  implements IAdapterFactory{
	
	private static Class[] PROPERTIES= new Class[] {
		IResource.class,
	};
	
	public Class[] getAdapterList() {
		return PROPERTIES;
	}
	
	public Object getAdapter(Object element, Class key) {
		if (IResource.class.equals(key)) {
			ContainerFolder containerFolder= (ContainerFolder)element;
			return containerFolder.enclosed();
		} 
		return null; 
	}
}
