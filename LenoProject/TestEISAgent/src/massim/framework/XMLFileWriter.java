package massim.framework;

import static massim.framework.util.DebugLog.LOGLEVEL_CRITICAL;
import static massim.framework.util.DebugLog.LOGLEVEL_ERROR;
import static massim.framework.util.DebugLog.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Observable;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMLFileWriter implements java.util.Observer {
	private File file;
	private File folder;
	private Transformer transformer;

	public XMLFileWriter(String prefix) {
		try {
			if (prefix == null){
				folder = null;
			} else {
				folder = new File(prefix);
			}
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT,"yes");
		} catch (TransformerConfigurationException e) {
			log(LOGLEVEL_CRITICAL,"error: Unable to instantiate XML transformer. Aborting...");
			e.printStackTrace();
		}
	}
	
	public XMLFileWriter() {
		this(null);
	}
	
	public void setFile(File file, boolean createFolder) {
		if (createFolder){
			new File(file.getParent()).mkdirs();
		}
		setFile(file);
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public void update(Observable o, Object arg) {
		XMLOutputObserver obs = (XMLOutputObserver) o;
		if (arg != null && arg instanceof File){
			setFile((File)arg, true);
		} else if (arg != null && arg instanceof String){
			setFile( new File(folder,(String)arg),true);
		}
		try {
			transformer.transform(new DOMSource(obs.getDocument()), new StreamResult(new FileOutputStream(getFile())));
		} catch (FileNotFoundException e) {
			log(LOGLEVEL_ERROR,"warning: unable to open file for writing");
			e.printStackTrace();
		} catch (TransformerException e) {
			log(LOGLEVEL_ERROR,"warning: transformer error");
			e.printStackTrace();
		}
	}
}
