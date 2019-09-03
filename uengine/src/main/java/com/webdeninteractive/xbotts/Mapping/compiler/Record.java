/*
-------------------------------------------------------------------
BIE is Copyright 2001-2004 Brunswick Corp.
-------------------------------------------------------------------
Please read the legal notices (docs/legal.txt) and the license
(docs/bie_license.txt) that came with this distribution before using
this software.
-------------------------------------------------------------------
*/
/*
 * RecordNode.java
 *
 * Created on August 12, 2002, 10:13 AM
 */

package com.webdeninteractive.xbotts.Mapping.compiler;
import com.webdeninteractive.xbotts.Mapping.macro.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import java.awt.datatransfer.*;
/** This would be called Element, but it would collide with w3c Element all over the place
 *
 * @author  bmadigan
 */
public class Record extends DefaultMutableTreeNode implements LinkableTreeNode {
	
	public final static int UNSET_KEY = 0;
	public final static int KEY = 1;
	
    
    /** Creates a new instance of RecordNode */
    public Record(String name) {
        this.name=name;
    }
    
    int keyType;
	    public int getKeyType(){
	    	return this.keyType;
	    }
		public void setKeyType(int keyType) {
			this.keyType = keyType;
		}
	
	Class classType;
		public Class getClassType() {
			return classType;
		}
		public void setClassType(Class classType) {
			this.classType = classType;
		}

    private String direction;
    public String getDirection() {
		return this.direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
    
    String name;
    public void setName(String name){
        this.name=name;
    }
    public String getName( ){
        return name;
    }
    String type;
    public void setType(String type){
        this.type=type;
    }
    public String getType( ){
        return type;
    }
    
    String nodeType;
    public void setNodeType(String type){
        this.nodeType = type;
    }
    public String getNodeType( ){
        return nodeType;
    }
    
    String typeName;
    public String getTypeName( ){
        return typeName;
    }
    public void setTypeName(String typeName){
        this.typeName=typeName;
    }
    String refName;
    public String getReferenceName( ){
        return refName;
    }
    public void setReferenceName( String refName ){
        this.refName = refName;
    }
    
    public String structure;
    public void setStructure(String struct){
        if(structure!=null){
            System.out.println("Structure is being modified unexpectedly. was :"+structure+" now:"+struct+" for "+name);
            return;
        }
        this.structure = struct;
    }
        
    public String getStructure( ){
        return structure;
    }
    
    String use;
    public void setUse(String use){
        this.use=use;
    }
    public String getUse( ){
        return use;
    }
    public String getStructureGeneric( ){
        int min=1;
        int max=1;
        boolean repeats = false;
        boolean optional = false;
        
        if(!(getMinOccurs( )==null||getMinOccurs( ).equals(""))){
            min = Integer.parseInt( getMinOccurs( ) );
        }

        if(!(getMaxOccurs( )==null||getMaxOccurs( ).equals(""))){
            if(getMaxOccurs ().equals("unbounded")||Integer.parseInt(getMaxOccurs( ))>1){
                repeats=true;
            }
        }

        
        if(min == 0) optional = true;
        
        if(getStructure( )!=null&&(getStructure( ).equals(Linkable.COMPLEX_TYPE)||
        getStructure( ).equals(Linkable.TEMPLATE))){
            if(optional&&repeats) return Linkable.OPTIONAL_REPEATING_GROUP;
            if(!optional&&repeats) return Linkable.REPEATING_GROUP;
            if(!optional&&!repeats) return Linkable.GROUP;
            if(optional&&!repeats) return Linkable.OPTIONAL_GROUP;
        }else{
            if(optional&&repeats) return Linkable.OPTIONAL_REPEATING_FIELD;
            if(optional&&!repeats) return Linkable.OPTIONAL_FIELD;
            if(!optional&&repeats) return Linkable.REPEATING_FIELD;
            if(!optional&&!repeats) return Linkable.FIELD;
        }
        return "error";
    }
    
    public LinkableTreeNode parent;
    
    //uengine    
    public Record parentRecord;
    
    public Record getParentRecord() {
		return parentRecord;
	}
	public void setParentRecord(Record parentRecord) {
		this.parentRecord = parentRecord;
	}
    
    /****
     *  methods from interface TreeNode
     */
    public boolean isLeaf() {
        return false;
    }
    
    Vector children = new Vector( );
    public java.util.Enumeration children() {
        return children.elements( );
    }
    
    public void add(Record rec){
    	children.add(rec);
    }
    
    public boolean getAllowsChildren() {
        return true;
    }
    
    public javax.swing.tree.TreeNode getChildAt(int index) {
        return (TreeNode) children.get(index);
    }
    
    public int getChildCount() {
        return children.size( );
    }
    
    public int getIndex(javax.swing.tree.TreeNode treeNode) {
        return children.indexOf(treeNode);
    }
    
    public javax.swing.tree.TreeNode getParent() {
        return (TreeNode) parent;
    }

	ArrayList impliedLinkSources = new ArrayList( );
    ArrayList impliedLinkTargets = new ArrayList( );
    Linkable hardLinkSource;
    Linkable hardLinkTarget;
    /**
     *  Methods from interface Linkable
     *
     */
    public void addImpliedLinkSource(Linkable l) {
        if(!impliedLinkSources.contains(l)){
            impliedLinkSources.add(l);
        }
    }
    
    public void addImpliedLinkTarget(Linkable l) {
        if(!impliedLinkTargets.contains(l)){
            impliedLinkTargets.add(l);
        }
    }
    
    public Linkable getHardLinkSource() {
        return hardLinkSource;
    }
    
    public Linkable getHardLinkTarget() {
        return hardLinkTarget;
    }
    
    public Linkable[] getImpliedLinkSources() {
        int size=impliedLinkSources.size( );
        Linkable[] la = new Linkable[size];
        for(int i=0;i<size;i++){
            la[i]=(Linkable)impliedLinkSources.get(i);
        }
        return la;
    }
    
    public Linkable[] getImpliedLinkTargets() {
        int size = impliedLinkTargets.size();
        Linkable[] la = new Linkable[size];
        for(int i=0;i<size;i++){
            la[i]=(Linkable)impliedLinkTargets.get(i);
        }
        return la;
    }
    
    public void setHardLinkSource(Linkable l) {
        this.hardLinkSource=l;
    }
    
    public void setHardLinkTarget(Linkable l) {
        this.hardLinkTarget=l;
    }
    
    public Linkable getChild(int index){
        return (Linkable)children.get(index);
    }
    public Linkable getChild(String name){
        /*
        if(name.startsWith("@")){//strip attribute token ()
            name=name.substring((name.indexOf("@")+1), name.length( ));
        }
         */
        Iterator iter = children.iterator( );
        Linkable child = null;
        if(name.indexOf("[")==-1){
            while(iter.hasNext( )){
                child = (Linkable) iter.next( );
                if(child.getName( ).equals(name)){
                    return child;
                }
            }
            return null;
        }else{
            int index = Integer.parseInt(name.substring(name.indexOf("[")+1, name.indexOf("]")));
            String n = name.substring(0, name.indexOf("["));
            int current=0;
            while(iter.hasNext( )){
                Linkable l =(Linkable) iter.next( );
                if(l.getName( ).equals(n)){
                    if(current==index) return l;
                    current++;
                }
            }
        }
        return child;
    }
    
    public void appendChild(Linkable child){
       appendChildRecord((Record)child);
    }
    
    public Linkable getOwner( ){
        return (Linkable)parent;
    }
    
    public Linkable[] getChildNodes( ){
        return (Linkable[]) children.toArray(empty);
    }

    public void appendChildRecord(Record child){
         child.setOwner(this);
         childRecordsMap.put(child.getName(), child);
         childRecords.add(child);
         children.add(child);
         childNameMap.put(child.getName( ), child);
    }
    
    public Record getChildRecord(String name){
        return (Record) childRecordsMap.get(name);
    }
    public Record getChildRecord(int index){
        return (Record) childRecords.get(index);
    }
    public ArrayList getChildRecords( ){
        return childRecords;
    }
    
    public Vector getChildren( ){
        return children;
    }
    
    public void removeChild(Linkable child){
        if(child instanceof Record){
            childRecords.remove(child);
        }
        children.remove(child);
    }
    
    public void setOwner(Linkable parent){
        this.parent=(LinkableTreeNode)parent;
    }
    
    public int getDepth( ){
        Record p=(Record)this.parent;
        int c = 0;
        while(p!=null){
            p=(Record)p.getOwner( );
            c++;
        }
        return c;
    }
    // ArrayList children = new ArrayList( ); //contains all children
    HashMap childNameMap = new HashMap( ); //map for all children
    ArrayList childRecords = new ArrayList( ); //contains only records
    HashMap aeMap = new HashMap( ); //atom name map
    HashMap childRecordsMap = new HashMap( ); //record name map
    
    
    public Object getTransferData(java.awt.datatransfer.DataFlavor df) throws java.awt.datatransfer.UnsupportedFlavorException, java.io.IOException {
        if(!df.equals(LINKABLE_FLAVOR)){
            throw new UnsupportedFlavorException(df);
        }
        return this;
    }
    
    public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }
    
    public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor df) {
        return df.equals(LINKABLE_FLAVOR);
    }
    
    public String toString( ){
        return name;
    }
    
    int rowNumber;
    public void setRow(int row) {
        this.rowNumber=row;
    }
    
    public int getRow() {
        return rowNumber;
    }
    
    boolean isRecursive = false;
    public void setIsRecursive(boolean value){
        this.isRecursive = value;
    }
    public boolean isRecursive( ){
        return isRecursive;
    }
    
    ArrayList errorsList = new ArrayList( );
    public void addError(String error){
        errorsList.add(error);
    }
    
    public ArrayList getErrorList( ){
        return errorsList;
    }
    
    String documentation;
    public void setDocumentation(String desc){
        this.documentation = desc;
    }
    public String getDocumentation( ){
        return documentation;
    }
    
    String extensionBase;
    public void setExtensionBase(String base){
        this.extensionBase=base;
    }
    public String getExtensionBase( ){
        return extensionBase;
    }
    
    
    /**
     * Corresponds to Schema element's minOccurs attribute
     */
    int minOccurence = 0;
    
    /**
     * Corresponds to Schema element's maxOccurs attribute
     */
    int maxOccurence = 0;
    /**
     * sets maxOccurence
     */
    public void setMaxOccurence(int maxOccurence){
        this.maxOccurence=maxOccurence;
    }
    /**
     * gets maxOccurence
     */
    public int getMaxOccurence( ){
        return maxOccurence;
    }
    /**
     * sets minOccurence
     */
    public void setMinOccurence( int minOccurence ){
        this.minOccurence = minOccurence;
    }
    
    /**
     * gets minOccurence
     */
    public int getMinOccurence( ){
        return minOccurence;
    }
    
    public String defaultValue;
    public String getDefault( ){
        return defaultValue;
    }
    public void setDefault(String defaultValue){
        this.defaultValue = defaultValue;
    }
    //remove, not used
    public int getX() {
        return 0;
    }
    
    public int getY() {
        return 0;
    }
    
    ArrayList functionLinks = new ArrayList( );
    public void addLinkFromExtensionFunction(Function func) {
        functionLinks.add(func);
    }
    ArrayList argLinks = new ArrayList( );
    public void addLinkToExtensionArgument(Argument arg) {
        argLinks.add(arg);
    }
    public ArrayList getExtensionFunctionSources( ){
        return functionLinks;
    }
    public ArrayList getExtensionArgumentTargets( ){
        return argLinks;
    }
    
    String maxOccurs="1"; //default as per w3c spec
    String minOccurs="1";
    public String getMaxOccurs( ){
        return maxOccurs;
    }
    public String getMinOccurs( ){
        return minOccurs;
    }
    public void setMaxOccurs(String m){
        this.maxOccurs=m;
    }
    public void setMinOccurs(String m){
        this.minOccurs = m;
    }
    
    /** the above methods for extension links are going away soon, use
     * these. Also, maybe these are not needed. this info can be found in
     *  MapToolDataModel as well.
     */
    
    public void addExtensionParameterTarget(Parameter param) {
        extParamTargets.add(param);
    }
    
    public void addFunctionCallSource(ExtensionFunction function) {
        functionCallSources.add(function);
    }
    
    public ArrayList getExtensionParameterTargets() {
        return extParamTargets;
    }
    
    public ArrayList getFunctionCallSources() {
        return functionCallSources;
    }
    
    public void removeExtensionParameterTarget(Parameter param) {
        extParamTargets.remove(param);
    }
    
    public void removeFunctionCallSource(ExtensionFunction function) {
        functionCallSources.remove(function);
    }
    
    
    //checks to see if this Linkable is linked.
    public boolean isLinked() {
        if((hardLinkTargets.size( )>0)
        ||(hardLinkSources.size( )>0)
        ||(getExtensionArgumentTargets( ).size()>0)
        ||(getExtensionFunctionSources( ).size()>0)
        ||(getExtensionParameterTargets( ).size()>0)
        ||(getFunctionCallSources( ).size()>0)
        ||(getSourceContexts( ).size()>0)
        ||(getTargetContexts( ).size()>0)) return true;
        return false;
    }
    
    public int getLinkType( ){
        if((hardLinkTargets.size( )>0)
        ||(hardLinkSources.size( )>0)
        ||(getExtensionArgumentTargets( ).size()>0)
        ||(getExtensionFunctionSources( ).size()>0)
        ||(getExtensionParameterTargets( ).size()>0)
        ||(getFunctionCallSources( ).size()>0)) return ATOMIC_LINK;
        if((getSourceContexts( ).size()>0)
        ||(getTargetContexts( ).size()>0)) return CONTEXT_LINK;
        return NOT_LINKED;
    }
    
    
    ArrayList hardLinkSources = new ArrayList ( );
    public void addHardLinkSource(Linkable source) {
        if(!hardLinkSources.contains(source)) hardLinkSources.add(source);
    }
    ArrayList hardLinkTargets = new ArrayList( );
    public void addHardLinkTarget(Linkable target) {
        if(!hardLinkTargets.contains(target)) hardLinkTargets.add(target);
    }
    
    public Linkable[] getHardLinkSources() {
        return (Linkable[]) hardLinkSources.toArray(empty);
    }
    
    public java.util.List getLinkTargets( ){
        return hardLinkTargets;
    }
    
    public java.util.List getLinkSources( ){
        return hardLinkSources;
    }
    
    Linkable [] empty = new Linkable[0];
    public Linkable[] getHardLinkTargets() {
        return (Linkable[]) hardLinkTargets.toArray(empty);
    }
    
    public boolean removeHardLinkSource(Linkable source) {
        return hardLinkSources.remove(source);
    }
    
    public boolean removeHardLinkTarget(Linkable target) {
        return  hardLinkTargets.remove(target);
    }
    
    ArrayList nToOneImpliedSources = new ArrayList();
    public void addNToOneImpliedLinkSource(Linkable l) {
        if(!nToOneImpliedSources.contains(l)) nToOneImpliedSources.add(l);
    }
    
    public Linkable[] getNToOneImpliedLinkSources() {
        return (Linkable[])nToOneImpliedSources.toArray(new Linkable[0]);
    }
    
    //
    public void insertNToOneImpliedLinkSource(int index, Linkable l) {
        if(!nToOneImpliedSources.contains(l)) nToOneImpliedSources.add(index, l);
    }
    
    java.util.List sourceContexts = new ArrayList( );

    public java.util.List getSourceContexts() {
        return sourceContexts;
    }
    
    java.util.List targetContexts = new ArrayList( );
    public java.util.List getTargetContexts() {
        return targetContexts;
    }
    
    public boolean is(Linkable compareTo) {
        if(!this.name.equals(compareTo.getName())) return false;
        LinkPath myPath = new LinkPath(this);
        LinkPath itsPath = new LinkPath(compareTo);
        if(myPath.equals(itsPath))return true;
        return false;
    }
    
    public Component getComponent() {
        return component;
    }
    
    java.awt.Component component;
    public void setComponent(Component c) {
        this.component=c;
    }
    
    ArrayList extParamTargets = new ArrayList( );
    ArrayList functionCallSources = new ArrayList( );
    
    
    Hashtable extendedProperties = new Hashtable();
		public Hashtable getExtendedProperties() {
			return extendedProperties;
		}
		public void setExtendedProperties(Hashtable extendedProperties) {
			this.extendedProperties = extendedProperties;
		}
		public Point getLinkPoint() {
			return null;
		}
}
