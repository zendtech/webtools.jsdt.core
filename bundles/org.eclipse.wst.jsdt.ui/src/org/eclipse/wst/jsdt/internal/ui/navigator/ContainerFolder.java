/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.wst.jsdt.internal.ui.navigator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;


/**
 * @author childsb
 *
 */
public class ContainerFolder {

		IFolder resource;	
		IJavaScriptProject project;
		
		public ContainerFolder(IFolder resource, IJavaScriptProject project){
			
			this.resource = resource;
			this.project = project;
		}
		
		public IFolder enclosed(){
			return this.resource;
		}
		public String toString() {
			return resource.toString();
			}

		public IJavaScriptProject getProject(){
			return this.project;
		}

		public boolean equals(Object obj) {
			if(obj instanceof IFolder){
				return this.resource.equals(obj);
			}
			if(obj instanceof ContainerFolder){
				ContainerFolder fol = (ContainerFolder)obj;
				return fol.resource==this.resource;
			}
			return false;
		}
		
}
