package aQute.bnd.osgi;

import java.io.*;
import java.util.regex.*;

import aQute.lib.io.*;

public class FileResource implements Resource, Closeable {
	static final int BUFFER_SIZE = IOConstants.PAGE_SIZE * 16;

	File	file;
	String	extra;
	boolean deleteOnClose;

	public FileResource(File file) {
		this.file = file;
	}

	/**
	 * Turn a resource into a file so that anything in the conversion is properly caught
	 * @param r
	 * @throws Exception
	 */
	public FileResource(Resource r) throws Exception {
		this.file = File.createTempFile("fileresource", ".resource");
		deleteOnClose(true);
		this.file.deleteOnExit();
		IO.copy(r.openInputStream(), this.file);
	}

	public InputStream openInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}

	public static void build(Jar jar, File directory, Pattern doNotCopy) {
		traverse(jar, directory.getAbsolutePath().length(), directory, doNotCopy);
	}

	@Override
	public String toString() {
		return file.getAbsolutePath();
	}

	public void write(OutputStream out) throws Exception {
		copy(this, out);
	}

	static synchronized void copy(Resource resource, OutputStream out) throws Exception {
		InputStream in = resource.openInputStream();
		try {
			byte buffer[] = new byte[BUFFER_SIZE];
			int size = in.read(buffer);
			while (size > 0) {
				out.write(buffer, 0, size);
				size = in.read(buffer);
			}
		}
		finally {
			in.close();
		}
	}

	static void traverse(Jar jar, int rootlength, File directory, Pattern doNotCopy) {
		if (doNotCopy != null && doNotCopy.matcher(directory.getName()).matches())
			return;
		jar.updateModified(directory.lastModified(), "Dir change " + directory);

		File files[] = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				traverse(jar, rootlength, files[i], doNotCopy);
			else {
				String path = files[i].getAbsolutePath().substring(rootlength + 1);
				if (File.separatorChar != '/')
					path = path.replace(File.separatorChar, '/');
				jar.putResource(path, new FileResource(files[i]), true);
			}
		}
	}

	public long lastModified() {
		return file.lastModified();
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public long size() {
		return (int) file.length();
	}

	public void close() throws IOException {
		if ( deleteOnClose) 
			file.delete();
	}

	public void deleteOnClose(boolean b) {
		deleteOnClose = b;
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if ( deleteOnClose) 
			file.delete();
		super.finalize();
	}
}
