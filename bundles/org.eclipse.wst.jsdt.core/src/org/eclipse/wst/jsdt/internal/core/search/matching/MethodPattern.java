/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.core.Logger;
import org.eclipse.wst.jsdt.internal.core.index.EntryResult;
import org.eclipse.wst.jsdt.internal.core.index.Index;
import org.eclipse.wst.jsdt.internal.core.util.QualificationHelpers;

/**
 * <p>Pattern used when adding functions to an index or searching an index for functions.</p>
 */
public class MethodPattern extends JavaSearchPattern  {

	protected static final char[][] REF_CATEGORIES = { METHOD_REF };
	protected static final char[][] REF_AND_DECL_CATEGORIES = { METHOD_REF, METHOD_DECL };
	protected static final char[][] DECL_CATEGORIES = { METHOD_DECL };
	protected static final char[][] FUNCTION_REF_AND_DECL_CATEGORIES = { METHOD_REF, FUNCTION_DECL, METHOD_DECL };
	protected static final char[][] FUNCTION_DECL_CATEGORIES = { FUNCTION_DECL, METHOD_DECL };
	
	/**
	 * <p><b>Required</b></p>
	 * 
	 * <p>Name of the function</p>
	 */
	public char[] selector;
	
	/**
	 * <p><b>Required</b></p>
	 * 
	 * <p>Number of specified parameters for this function.
	 * Must be equal to or greater then 0.</p>
	 */
	public int parameterCount;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Qualifications of the parameter types for this function.</p>
	 * <p>This should have the same length as {@link #parameterCount}, or
	 * <code>null</code> if {@link #parameterCount} is 0</p>
	 * 
	 * <p><b>Note:</b> If this field is defined then the {@link #parameterSimpleNames} must
	 * also be defined.</p>
	 * 
	 * @see #parameterSimpleNames
	 */
	public char[][] parameterQualifications;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Simple names of the parameter types for this function.</p>
	 * <p>This should have the same length as {@link #parameterCount}, or
	 * <code>null</code> if {@link #parameterCount} is 0</p>
	 * 
	 * <p><b>Note:</b> If this field is defined then the {@link #parameterQualifications}
	 * filed can be defined, but does not have to be.</p>
	 * 
	 * @see #parameterQualifications
	 */
	public char[][] parameterSimpleNames;
	
	/**
	 * <p><b>Required</b> if {@link #parameterCount} is greater then 0</p>
	 * 
	 * <p>Names of the defined parameters for this function.</p>
	 * <p>This should have the same length as {@link #parameterCount}, or
	 * <code>null</code> if {@link #parameterCount} is 0</p>
	 */
	public char[][] parameterNames;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Qualification of the return type of this function.</p>
	 * 
	 * <p><b>Note:</b> If this field is defined then the {@link #returnSimpleName} must
	 * also be defined.</p>
	 */
	public char[] returnQualification;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Simple name of the return type of this function.</p>
	 * 
	 * <p><b>Note:</b> If this field is defined then the {@link #returnQualification}
	 * filed can be defined, but does not have to be.</p>
	 */
	public char[] returnSimpleName;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Qualification of the declaring type containing this function.</p>
	 * 
	 * <p><b>Note:</b> If this field is defined then the {@link #declaringSimpleName} must
	 * also be defined.</p>
	 * 
	 * @see #declaringSimpleName
	 */
	public char[] declaringQualification;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Simple name of the declaring type containing this function.</p>
	 * 
	 * <p><b>Note:</b> If this field is defined then the {@link #declaringQualification}
	 * can be defined, but does not have to be.</p>
	 * 
	 * @see #declaringQualification
	 */
	public char[] declaringSimpleName;
	
	/**
	 * <p><b>Optional</b></p>
	 * 
	 * <p>Any modifiers for this function.</p>
	 * 
	 * @see ClassFileConstants
	 */
	public int modifiers;
	
	/**
	 * <p>When using this pattern to do a search <code>true</code> to
	 * find function declarations that match this pattern, <code>false</code> otherwise.</p>
	 */
	protected boolean findDeclarations;
	
	/**
	 * <p>When using this pattern to do a search <code>true</code> to
	 * find function references that match this pattern, <code>false</code> otherwise.</p>
	 */
	protected boolean findReferences;
	
	/**
	 * <p><code>true</code> if this pattern represents a function,
	 * <code>false</code> otherwise.</p>
	 * 
	 * <p><b>NOTE:</b> this whole concept should be removed, a function is a function is a function.</p>
	 */
	protected boolean isFunction;

	/**
	 * <p>Internal constructor for creating plank patterns</p>
	 *
	 * @param matchRule match rule used when comparing this pattern to search results
	 * @param isFunction <code>true</code> if this pattern represents a function,
	 * <code>false</code> otherwise
	 */
	MethodPattern(int matchRule, boolean isFunction) {
		super(METHOD_PATTERN, matchRule);
		this.isFunction=isFunction;
	}
	
	/**
	 * <p>Useful constructor when creating a pattern to search for index matches
	 * while doing content assist.</p>
	 *
	 * @param findDeclarations when using this pattern to do a search <code>true</code> to
	 * find function declarations that match this pattern, <code>false</code> otherwise.
	 * @param findReferences hen using this pattern to do a search <code>true</code> to
	 * find function references that match this pattern, <code>false</code> otherwise
	 * @param isFunction <code>true</code> if this pattern represents a function,
	 * <code>false</code> otherwise
	 * @param selector pattern for the name of the function
	 * @param selectorMatchRule match rule used when comparing this pattern to search results.
	 * This dictates what type of pattern is present, if any, in the specified <code>selector</code>
	 * 
	 * @see SearchPattern
	 */
	public MethodPattern(boolean findDeclarations,
			boolean findReferences,
			boolean isFunction,
			char[] selector,
			int selectorMatchRule) {
		
		this(findDeclarations, findReferences, isFunction, selector,
				null, null, null, null, null, null,
				selectorMatchRule);
	}
	
	/**
	 * <p>Useful constructor for finding index matches based on content assist pattern.</p>
	 *
	 * @param findDeclarations when using this pattern to do a search <code>true</code> to
	 * find function declarations that match this pattern, <code>false</code> otherwise.
	 * @param findReferences hen using this pattern to do a search <code>true</code> to
	 * find function references that match this pattern, <code>false</code> otherwise
	 * @param selector pattern for the name of the function
	 * @param declaringType optional declaring type that the given selector must be
	 * defined on to be a valid match, or <code>null</code> to specify the function is not
	 * defined on a type
	 * @param selectorMatchRule match rule used when comparing this pattern to search results.
	 * This dictates what type of pattern is present, if any, in the specified <code>selector</code>
	 */
	public MethodPattern(boolean findDeclarations, boolean findReferences,
			char[] selector, char[] declaringType,
			int selectorMatchRule){
		
		this(selectorMatchRule, true);
		
		this.findDeclarations = findDeclarations;
		this.findReferences = findReferences;
		this.selector = isCaseSensitive() ? selector : CharOperation.toLowerCase(selector);
		this.parameterCount = -1;
		
		char[][] seperatedDeclaringType = QualificationHelpers.seperateFullyQualifedName(declaringType);
		this.declaringQualification = isCaseSensitive() ?
				seperatedDeclaringType[QualificationHelpers.QULIFIERS_INDEX] : CharOperation.toLowerCase(seperatedDeclaringType[QualificationHelpers.QULIFIERS_INDEX]);
		this.declaringSimpleName = isCaseSensitive() ?
				seperatedDeclaringType[QualificationHelpers.SIMPLE_NAMES_INDEX] : CharOperation.toLowerCase(seperatedDeclaringType[QualificationHelpers.SIMPLE_NAMES_INDEX]);
	}
	
	/**
	 * <p>Constructor to create a pattern that accepts all possible information about a function.</p>
	 *
	 * @param findDeclarations
	 * @param findReferences
	 * @param isFunction
	 * @param selector
	 * @param declaringQualification
	 * @param declaringSimpleName
	 * @param returnQualification
	 * @param returnSimpleName
	 * @param parameterQualifications
	 * @param parameterSimpleNames
	 * @param matchRule
	 */
	public MethodPattern(
		boolean findDeclarations,
		boolean findReferences,
		boolean isFunction,
		char[] selector,
		char[][] parameterQualifications,
		char[][] parameterSimpleNames,
		char[] returnQualification,
		char[] returnSimpleName,
		char[] declaringQualification,
		char[] declaringSimpleName,
		int matchRule) {

		this(matchRule,isFunction);

		this.findDeclarations = findDeclarations;
		this.findReferences = findReferences;

		this.selector = (isCaseSensitive() || isCamelCase())  ? selector : CharOperation.toLowerCase(selector);
		this.declaringQualification = isCaseSensitive() ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
		this.declaringSimpleName = isCaseSensitive() ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
		this.returnQualification = isCaseSensitive() ? returnQualification : CharOperation.toLowerCase(returnQualification);
		this.returnSimpleName = isCaseSensitive() ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);
		if (parameterSimpleNames != null) {
			this.parameterCount = parameterSimpleNames.length;
			this.parameterQualifications = new char[this.parameterCount][];
			this.parameterSimpleNames = new char[this.parameterCount][];
			for (int i = 0; i < this.parameterCount; i++) {
				this.parameterQualifications[i] = isCaseSensitive() ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
				this.parameterSimpleNames[i] = isCaseSensitive() ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
			}
		} else {
			this.parameterCount = -1;
		}
		
		((InternalSearchPattern)this).mustResolve = false;
	}
	
	/**
	 * <p>Given an index key created by this class decodes that key into the
	 * various fields of this pattern.</p>
	 * 
	 * @param key to decode into the fields of this pattern
	 * 
	 * @see #createIndexKey(char[], int)
	 * @see #createIndexKey(char[], int, char[][], char[][], char[], char[], int)
	 * @see #createSearchIndexKey(char[], char[], char[], int)
	 * @see #createSearchIndexKey(char[], int, char[][], char[][], char[][], char[], char[], char[], char[], int)
	 */
	public void decodeIndexKey(char[] key) {
		char[][] seperated = CharOperation.splitOn(SEPARATOR, key);
		
		//get the selector
		this.selector = seperated[0];
		
		//get the parameter count
		this.parameterCount = Integer.parseInt(new String(seperated[1]));
		
		//get parameter types
		char[][][] parameterTypes = QualificationHelpers.seperateFullyQualifiedNames(seperated[2], this.parameterCount);
		this.parameterQualifications = parameterTypes[QualificationHelpers.QULIFIERS_INDEX];
		this.parameterSimpleNames = parameterTypes[QualificationHelpers.SIMPLE_NAMES_INDEX];
		
		//get parameter names
		char[][] parameterNames = CharOperation.splitOn(PARAMETER_SEPARATOR, seperated[3]);
		if(parameterNames.length > 0) {
			this.parameterNames = parameterNames;
		} else {
			this.parameterNames = null;
		}
		
		//get the return type
		char[][] returnType = QualificationHelpers.seperateFullyQualifedName(seperated[4]);
		this.returnQualification = returnType[QualificationHelpers.QULIFIERS_INDEX];
		this.returnSimpleName = returnType[QualificationHelpers.SIMPLE_NAMES_INDEX];
		
		//get the declaration type
		char[][] declaringType = QualificationHelpers.seperateFullyQualifedName(seperated[5]);
		this.declaringQualification = declaringType[QualificationHelpers.QULIFIERS_INDEX];
		this.declaringSimpleName = declaringType[QualificationHelpers.SIMPLE_NAMES_INDEX];
		
		//get the modifiers
		this.modifiers = seperated[6][0] + seperated[6][1];
	}
	
	public SearchPattern getBlankPattern() {
		return new MethodPattern(R_EXACT_MATCH | R_CASE_SENSITIVE,isFunction);
	}
	public char[][] getIndexCategories() {
		if (this.findReferences)
			return this.findDeclarations ?
					(isFunction ? FUNCTION_REF_AND_DECL_CATEGORIES : REF_AND_DECL_CATEGORIES)
					: REF_CATEGORIES;
		if (this.findDeclarations)
			return isFunction ? FUNCTION_DECL_CATEGORIES : DECL_CATEGORIES;
		return CharOperation.NO_CHAR_CHAR;
	}

	boolean hasMethodParameters() {
		return this.parameterCount > 0;
	}
	boolean isPolymorphicSearch() {
		return this.findReferences;
	}
	
	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchPattern#matchesDecodedKey(org.eclipse.wst.jsdt.core.search.SearchPattern)
	 */
	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
		boolean matches = false;
		if(decodedPattern instanceof MethodPattern) {
			MethodPattern pattern = (MethodPattern) decodedPattern;
			
			matches = matchesName(this.selector, pattern.selector)
					&& (this.parameterCount == pattern.parameterCount || this.parameterCount == -1)
					&& matchesName(this.returnQualification, pattern.returnQualification)
					&& matchesName(this.returnSimpleName, pattern.returnSimpleName)
					&& matchesName(this.declaringQualification, pattern.declaringQualification)
					&& matchesName(this.declaringSimpleName, pattern.declaringSimpleName);
			
			if(matches) {
				for(int i = 0; i < this.parameterCount && matches; ++i) {
					matches = matches
							&& matchesName(this.parameterQualifications[i], pattern.parameterQualifications[i])
							&& matchesName(this.parameterSimpleNames[i], pattern.parameterSimpleNames[i]);
				}
			}
		}
		
		return matches;
	}
	
	/**
	 * @see org.eclipse.wst.jsdt.internal.core.search.matching.InternalSearchPattern#queryIn(org.eclipse.wst.jsdt.internal.core.index.Index)
	 */
	EntryResult[] queryIn(Index index) throws IOException {
		char[] key = this.selector; // can be null
		int matchRule = getMatchRule();
	
		int matchRuleToUse = matchRule;
		switch(getMatchMode()) {
			case R_EXACT_MATCH :
				key = createSearchIndexKey(this.selector, this.parameterCount,
						this.parameterQualifications, this.parameterSimpleNames,
						this.parameterNames, this.returnQualification, this.returnSimpleName,
						this.declaringQualification, this.declaringSimpleName, this.modifiers);
				break;
			case R_PREFIX_MATCH :
				char[] selector = CharOperation.concat(this.selector, ONE_STAR);
				
				key = createSearchIndexKey(selector,
						this.declaringQualification, this.declaringSimpleName,
						this.modifiers);
				
				//the prefix match refers to the selector, but to do the actual match need to use a pattern
				matchRuleToUse = R_PATTERN_MATCH;
				break;
			case R_PATTERN_MATCH :
				key = createSearchIndexKey(this.selector,
						this.declaringQualification, this.declaringSimpleName,
						this.modifiers);
				break;
			case R_REGEXP_MATCH :
				// TODO implement regular expression match
				Logger.log(Logger.WARNING, "Regular expression matching is not yet implimented for MethodPattern");
				break;
		}
	
		return index.query(getIndexCategories(), key, matchRuleToUse); // match rule is irrelevant when the key is null
	}
	
	/**
	 * @see org.eclipse.wst.jsdt.internal.core.search.matching.JavaSearchPattern#print(java.lang.StringBuffer)
	 */
	protected StringBuffer print(StringBuffer output) {
		if (this.findDeclarations) {
			output.append(this.findReferences
				? "MethodCombinedPattern: " //$NON-NLS-1$
				: "MethodDeclarationPattern: "); //$NON-NLS-1$
		} else {
			output.append("MethodReferencePattern: "); //$NON-NLS-1$
		}
		if (declaringQualification != null)
			output.append(declaringQualification).append('.');
		if (declaringSimpleName != null)
			output.append(declaringSimpleName).append('.');
		else if (declaringQualification != null)
			output.append("*."); //$NON-NLS-1$

		if (selector != null)
			output.append(selector);
		else
			output.append("*"); //$NON-NLS-1$
		output.append('(');
		if (parameterSimpleNames == null) {
			output.append("..."); //$NON-NLS-1$
		} else {
			for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				if (parameterQualifications[i] != null) output.append(parameterQualifications[i]).append('.');
				if (parameterSimpleNames[i] == null) output.append('*'); else output.append(parameterSimpleNames[i]);
			}
		}
		output.append(')');
		if (returnQualification != null)
			output.append(" --> ").append(returnQualification).append('.'); //$NON-NLS-1$
		else if (returnSimpleName != null)
			output.append(" --> "); //$NON-NLS-1$
		if (returnSimpleName != null)
			output.append(returnSimpleName);
		else if (returnQualification != null)
			output.append("*"); //$NON-NLS-1$
		return super.print(output);
	}
	
	/**
	 * <p>Create an index key from a selector and a parameter count.</p>
	 * 
	 * <p><b>Note</b> Currently used to index function references, but the
	 * validity of this use is questionable.</p>
	 * 
	 * @param selector
	 * @param parameterCount
	 * 
	 * @return a function index key created from a selector and a parameter count
	 */
	public static char[] createIndexKey(char[] selector, int parameterCount) {
		return createIndexKey(selector, parameterCount, null, null, null, null, 0);
	}
	
	/**
	 * <p>Creates an index key based on the given function definition information.</p>
	 * 
	 * <p><b>Key Syntax</b>:
	 * <code>selector/parameterCount/parameterFullTypeNames/paramaterNames/returnFulLTypeName/declaringFullTypeName</code></p>
	 * 
	 * <p>
	 * <b>Examples:</b><ul>
	 * <li><code>myFunction/0////</code> - function with no parameters and no return type</li>
	 * <li><code>myFunction/0///String/</code> - function with no parameters with a return type</li>
	 * <li><code>myFunction/0////foo.bar.Type</code> - function on a type with no parameters and no return type</li>
	 * <li><code>myFunction/0///String/foo.bar.Type</code> - function on a type with no parameters with a return type </li>
	 * <li><code>myFunction/2//param1,param2//</code> - function with no parameter types, with parameter names with no return type</li>
	 * <li><code>myFunction/2//param1,param2/String/</code> - function with no parameter types, with parameter names with a return type</li>
	 * <li><code>myFunction/2//param1,param2//foo.bar.Type</code> - function on a type with no parameter types, with parameter  names with no return type</li>
	 * <li><code>myFunction/2//param1,param2/String/foo.bar.Type</code> - function on a type with no parameter types, with parameter names with a return type</li>
	 * <li><code>myFunction/2/String,Number/param1,param2//</code> - function with parameter types and names with no return type</li>
	 * <li><code>myFunction/2/String,Number/param1,param2/String/</code> - function with parameter types and names with a return type</li>
	 * <li><code>myFunction/2/String,Number/param1,param2//foo.bar.Type</code> - function on a type with parameter types and names with no return type</li>
	 * <li><code>myFunction/2/String,Number/param1,param2/String/foo.bar.Type</code> - function on a type with parameter types and names with a return type</li>
	 * <li><code>myFunction/2/,Number/param1,param2//</code> - function where only one of the parameters has a type</li>
	 * <li><code>myFunction/2/,Number/param1,param2/String/</code> - function where only one of the parameters has a type with a return type</li>
	 * <li><code>myFunction/2/,Number/param1,param2//foo.bar.Type</code> - function on a type where only one of the parameters has a type</li>
	 * <li><code>myFunction/2/,Number/param1,param2/String/foo.bar.Type</code> - function on a type where only one of the parameters has a type with a return type</li>
	 * </ul></p>
	 * 
	 * @param selector
	 * @param parameterCount
	 * @param declaringFullTypeName
	 * @param returnFullTypeName
	 * @param parameterFullTypeNames
	 * @param parameterNames
	 * @param modifiers
	 * 
	 * @see #decodeIndexKey(char[])
	 * 
	 * @return a key that can be put in an index or used to search an index for functions
	 */
	public static char[] createIndexKey(char[] selector, int parameterCount,
			char[][] parameterFullTypeNames,
			char[][] parameterNames,
			char[] declaringFullTypeName,
			char[] returnFullTypeName,
			int modifiers) {
		
		char[] indexKey = null;
		
		if(selector != null  && selector.length > 0) {
			char[] paramaterCountChars = null;
			char[] parameterTypesChars = CharOperation.NO_CHAR;
			char[] parameterNamesChars = CharOperation.NO_CHAR;
			
			
			//get param types
			if (parameterFullTypeNames != null) {
				parameterTypesChars = CharOperation.concatWith(parameterFullTypeNames, PARAMETER_SEPARATOR, false);
			}
			
			//get param names
			if (parameterNames != null) {
				parameterNamesChars = CharOperation.concatWith(parameterNames, PARAMETER_SEPARATOR);
			}
			
			//use pre-made char array for arg counts less then 10, else build a new one
			if(parameterCount >= 0) {
				paramaterCountChars = parameterCount < 10 ?
						COUNTS[parameterCount] : (SEPARATOR + String.valueOf(parameterCount)).toCharArray();
			} else {
				paramaterCountChars = CharOperation.concat(new char[] {SEPARATOR}, ONE_STAR);
			}
			
				
			//get lengths
			int parameterTypesLength = (parameterTypesChars == null ? 0 : parameterTypesChars.length);
			int parameterNamesLength = (parameterNamesChars == null ? 0 : parameterNamesChars.length);
			int returnTypeLength = (returnFullTypeName == null ? 0 : returnFullTypeName.length);
			int delaringTypeLength = declaringFullTypeName == null ? 0 : declaringFullTypeName.length;
			
			int resultLength = selector.length + paramaterCountChars.length
					+ 1 + parameterTypesLength
					+ 1 + parameterNamesLength
					+ 1 + returnTypeLength
					+ 1 + delaringTypeLength
					+ 3; //modifiers
			
			//create result char array
			indexKey = new char[resultLength];
			
			//add type name to result
			int pos = 0;
			System.arraycopy(selector, 0, indexKey, pos, selector.length);
			pos += selector.length;
			
			//add param count to result
			if (paramaterCountChars.length > 0) {
				System.arraycopy(paramaterCountChars, 0, indexKey, pos, paramaterCountChars.length);
				pos += paramaterCountChars.length;
			}
			
			//add param types
			indexKey[pos++] = SEPARATOR;
			if (parameterTypesLength > 0) {
				System.arraycopy(parameterTypesChars, 0, indexKey, pos, parameterTypesLength);
				pos += parameterTypesLength;
			}
			
			//add param names
			indexKey[pos++] = SEPARATOR;
			if (parameterNamesLength > 0) {
				System.arraycopy(parameterNamesChars, 0, indexKey, pos, parameterNamesLength);
				pos += parameterNamesLength;
			}
			
			//add return type
			indexKey[pos++] = SEPARATOR;
			if(returnTypeLength > 0) {
				System.arraycopy(returnFullTypeName, 0, indexKey, pos, returnTypeLength);
				pos += returnTypeLength;
			}
			
			//add declaring type
			indexKey[pos++] = SEPARATOR;
			if(delaringTypeLength > 0) {
				System.arraycopy(declaringFullTypeName, 0, indexKey, pos, delaringTypeLength);
				pos += delaringTypeLength;
			}
			
			//add modifiers
			indexKey[pos++] = SEPARATOR;
			indexKey[pos++] = (char) modifiers;
			indexKey[pos++] = (char) (modifiers>>16);
		}
			
		return indexKey;
	}
	
	/**
	 * <p>Create an index key for search the index for any function that matches the given selector
	 * pattern, on the optionally defined declaring type, with the given modifiers.</p>
	 * 
	 * @param selectorPattern
	 * @param declarationType
	 * @param modifiers
	 * 
	 * @return
	 */
	private static char[] createSearchIndexKey(char[] selectorPattern,
			char[] declaringQualification, char[] declaringSimpleName, int modifiers) {
		
		char[] declaringFullTypeName = null;
		if(declaringSimpleName != null) {
			declaringFullTypeName = QualificationHelpers.createFullyQualifiedName(declaringQualification, declaringSimpleName);
		}
		
		return createIndexKey(selectorPattern, -1,
				ONE_STAR_CHAR,
				ONE_STAR_CHAR,
				declaringFullTypeName,
				ONE_STAR,
				modifiers);
	}
	
	/**
	 * <p>Used to create an index key to search for a specific function.</p>
	 * 
	 * @param selector
	 * @param parameterCount
	 * @param parameterQualifications
	 * @param parameterSimpleNames
	 * @param parameterNames
	 * @param returnQualification
	 * @param returnSimpleName
	 * @param declaringQualification
	 * @param declaringSimpleName
	 * @param modifiers
	 * @return
	 */
	private static char[] createSearchIndexKey(char[] selector, int parameterCount,
			char[][] parameterQualifications,
			char[][] parameterSimpleNames,
			char[][] parameterNames,
			char[] returnQualification,
			char[] returnSimpleName,
			char[] declaringQualification,
			char[] declaringSimpleName,
			int modifiers) {
		
		//create fully qualified type names
		char[] declaringFullTypeName = QualificationHelpers.createFullyQualifiedName(declaringQualification, declaringSimpleName);
		char[] returnFullTypeName = QualificationHelpers.createFullyQualifiedName(returnQualification, returnSimpleName);
		char[][] parameterFullTypeNames = QualificationHelpers.createFullyQualifiedNames(parameterQualifications, parameterSimpleNames);
		
		return createIndexKey(selector, parameterCount, parameterFullTypeNames,
				parameterNames, declaringFullTypeName, returnFullTypeName, modifiers);
	}
}
