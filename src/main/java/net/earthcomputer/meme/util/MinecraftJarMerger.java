package net.earthcomputer.meme.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class MinecraftJarMerger {

	private static final String SIDE_ONLY_CLASS_DESC = "Lnet/earthcomputer/meme/SideOnly;";
	private static final String SIDE_CLASS_DESC = "Lnet/earthcomputer/meme/EnumSide;";
	private static final String SIDE_CLIENT_NAME = "CLIENT";
	private static final String SIDE_SERVER_NAME = "SERVER";

	private File clientJar;
	private File serverJar;
	private File outputJar;

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("args must be of length 3");
			System.exit(1);
			return;
		}

		File clientJar = new File(args[0]);
		File serverJar = new File(args[1]);
		File outputJar = new File(args[2]);

		try {
			new MinecraftJarMerger(clientJar, serverJar, outputJar).mergeJars();
		} catch (IOException e) {
			System.err.println("An I/O error occurred");
			e.printStackTrace();
			System.exit(1);
			return;
		}
	}

	public MinecraftJarMerger(File clientJar, File serverJar, File outputJar) {
		this.clientJar = clientJar;
		this.serverJar = serverJar;
		this.outputJar = outputJar;
	}

	public static void downloadClient(File dest) throws IOException {
		IOUtils.download(
				new URL("https://launcher.mojang.com/mc/game/1.11/client/780e46b3a96091a7f42c028c615af45974629072/client.jar"),
				dest);
	}

	public static void downloadServer(File dest) throws IOException {
		IOUtils.download(
				new URL("https://launcher.mojang.com/mc/game/1.11/server/48820c84cb1ed502cb5b2fe23b8153d5e4fa61c0/server.jar"),
				dest);
	}

	public void mergeJars() throws IOException {
		File strippedServerJar = createStrippedServerJar();
		System.out.println("Merging jars");
		ZipFile clientJarFile = new ZipFile(clientJar);
		ZipFile serverJarFile = new ZipFile(strippedServerJar);
		Set<String> allEntries = new LinkedHashSet<>();

		Enumeration<? extends ZipEntry> entries = clientJarFile.entries();
		while (entries.hasMoreElements()) {
			allEntries.add(entries.nextElement().getName());
		}
		entries = serverJarFile.entries();
		while (entries.hasMoreElements()) {
			String entryName = entries.nextElement().getName();
			if (!allEntries.contains(entryName)) {
				allEntries.add(entryName);
			}
		}

		ZipOutputStream jarOutputStream = new ZipOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputJar)));

		for (String entryName : allEntries) {
			if (entryName.startsWith("META-INF/")) {
				continue;
			}
			jarOutputStream.putNextEntry(new ZipEntry(entryName));
			if (!entryName.endsWith(".class")) {
				System.out.println("Copying " + entryName);
				InputStream is;
				ZipEntry jarEntry = clientJarFile.getEntry(entryName);
				if (jarEntry != null) {
					is = clientJarFile.getInputStream(jarEntry);
				} else {
					jarEntry = serverJarFile.getEntry(entryName);
					is = serverJarFile.getInputStream(jarEntry);
				}
				IOUtils.copyStreams(is, jarOutputStream);
			} else {
				System.out.println("Copying " + entryName);
				ZipEntry jarEntry = clientJarFile.getEntry(entryName);
				InputStream clientIs = jarEntry == null ? null : clientJarFile.getInputStream(jarEntry);
				jarEntry = serverJarFile.getEntry(entryName);
				InputStream serverIs = jarEntry == null ? null : serverJarFile.getInputStream(jarEntry);
				compareClassFiles(clientIs, serverIs, jarOutputStream);
			}
			jarOutputStream.closeEntry();
		}
		jarOutputStream.flush();
		jarOutputStream.close();
		clientJarFile.close();
		serverJarFile.close();
		System.out.println("Done");
	}

	private File createStrippedServerJar() throws IOException {
		System.out.println("Creating stripped server jar");
		File strippedServerJar = File.createTempFile("serverJar", ".jar");
		strippedServerJar.deleteOnExit();
		ZipFile serverJarFile = new ZipFile(serverJar);
		ZipOutputStream strippedServerJarOutputStream = new ZipOutputStream(
				new BufferedOutputStream(new FileOutputStream(strippedServerJar)));
		Enumeration<? extends ZipEntry> serverJarEntries = serverJarFile.entries();
		while (serverJarEntries.hasMoreElements()) {
			ZipEntry entryIn = serverJarEntries.nextElement();
			String entryName = entryIn.getName();
			if ((!entryName.contains("/")
					&& (entryName.endsWith(".class") || entryName.equals("yggdrasil_session_pubkey.der")))
					|| entryName.equals("net/") || entryName.startsWith("net/minecraft/")) {
				ZipEntry entryOut = new ZipEntry(entryName);
				strippedServerJarOutputStream.putNextEntry(entryOut);
				IOUtils.copyStreams(serverJarFile.getInputStream(entryIn), strippedServerJarOutputStream);
				strippedServerJarOutputStream.closeEntry();
			}
		}
		serverJarFile.close();
		strippedServerJarOutputStream.flush();
		strippedServerJarOutputStream.close();
		return strippedServerJar;
	}

	private void compareClassFiles(InputStream clientIs, InputStream serverIs, OutputStream os) throws IOException {
		if (serverIs == null) {
			ClassReader reader = new ClassReader(clientIs);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			List<AnnotationNode> anns = node.visibleAnnotations;
			if (anns == null) {
				anns = new ArrayList<>();
				node.visibleAnnotations = anns;
			}
			addSideOnlyAnnotation(anns, SIDE_CLIENT_NAME);

			ClassWriter writer = new ClassWriter(0);
			node.accept(writer);
			os.write(writer.toByteArray());
		} else if (clientIs == null) {
			ClassReader reader = new ClassReader(serverIs);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			List<AnnotationNode> anns = node.visibleAnnotations;
			if (anns == null) {
				anns = new ArrayList<>();
				node.visibleAnnotations = anns;
			}
			addSideOnlyAnnotation(anns, SIDE_SERVER_NAME);

			ClassWriter writer = new ClassWriter(0);
			node.accept(writer);
			os.write(writer.toByteArray());
		} else {
			ClassReader reader = new ClassReader(clientIs);
			ClassNode clientClass = new ClassNode();
			reader.accept(clientClass, 0);

			reader = new ClassReader(serverIs);
			ClassNode serverClass = new ClassNode();
			reader.accept(serverClass, 0);

			{
				// Get all fields from both classes
				Set<String> clientOnlyFields = new LinkedHashSet<>();
				Set<String> serverOnlyFields = new LinkedHashSet<>();
				for (FieldNode field : clientClass.fields) {
					clientOnlyFields.add(field.name);
				}
				for (FieldNode field : serverClass.fields) {
					serverOnlyFields.add(field.name);
				}
				// Eliminate the fields that appear in both
				Iterator<String> fieldItr = clientOnlyFields.iterator();
				while (fieldItr.hasNext()) {
					String fieldName = fieldItr.next();
					if (serverOnlyFields.contains(fieldName)) {
						fieldItr.remove();
						serverOnlyFields.remove(fieldName);
					}
				}

				// Add the annotations to the class
				for (FieldNode field : clientClass.fields) {
					if (clientOnlyFields.contains(field.name)) {
						List<AnnotationNode> anns = field.visibleAnnotations;
						if (anns == null) {
							anns = new ArrayList<>();
							field.visibleAnnotations = anns;
						}
						addSideOnlyAnnotation(anns, SIDE_CLIENT_NAME);
					}
				}
				for (FieldNode field : serverClass.fields) {
					if (serverOnlyFields.contains(field.name)) {
						clientClass.fields.add(field);
						List<AnnotationNode> anns = field.visibleAnnotations;
						if (anns == null) {
							anns = new ArrayList<>();
							field.visibleAnnotations = anns;
						}
						addSideOnlyAnnotation(anns, SIDE_SERVER_NAME);
					}
				}
			}
			{
				// Get all the methods from both classes
				Set<MethodSignature> clientOnlyMethods = new LinkedHashSet<>();
				Set<MethodSignature> serverOnlyMethods = new LinkedHashSet<>();
				for (MethodNode method : clientClass.methods) {
					clientOnlyMethods.add(new MethodSignature(method.name, method.desc));
				}
				for (MethodNode method : serverClass.methods) {
					serverOnlyMethods.add(new MethodSignature(method.name, method.desc));
				}
				// Eliminate the methods that appear in both
				Iterator<MethodSignature> methodItr = clientOnlyMethods.iterator();
				while (methodItr.hasNext()) {
					MethodSignature methodSignature = methodItr.next();
					if (serverOnlyMethods.contains(methodSignature)) {
						methodItr.remove();
						serverOnlyMethods.remove(methodSignature);
					}
				}

				// Add the annotations to the class
				for (MethodNode method : clientClass.methods) {
					if (clientOnlyMethods.contains(new MethodSignature(method.name, method.desc))) {
						List<AnnotationNode> anns = method.visibleAnnotations;
						if (anns == null) {
							anns = new ArrayList<>();
							method.visibleAnnotations = anns;
						}
						addSideOnlyAnnotation(anns, SIDE_CLIENT_NAME);
					}
				}
				for (MethodNode method : serverClass.methods) {
					if (serverOnlyMethods.contains(new MethodSignature(method.name, method.desc))) {
						clientClass.methods.add(method);
						List<AnnotationNode> anns = method.visibleAnnotations;
						if (anns == null) {
							anns = new ArrayList<>();
							method.visibleAnnotations = anns;
						}
						addSideOnlyAnnotation(anns, SIDE_SERVER_NAME);
					}
				}
			}

			ClassWriter writer = new ClassWriter(0);
			clientClass.accept(writer);
			os.write(writer.toByteArray());
		}
	}

	private void addSideOnlyAnnotation(List<AnnotationNode> anns, String side) {
		AnnotationNode annotation = new AnnotationNode(SIDE_ONLY_CLASS_DESC);
		anns.add(annotation);
		List<Object> values = new ArrayList<>(2);
		annotation.values = values;
		values.add("value");
		values.add(new String[] { SIDE_CLASS_DESC, side });
	}

	private static class MethodSignature {
		private String name;
		private String desc;

		public MethodSignature(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((desc == null) ? 0 : desc.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof MethodSignature))
				return false;
			MethodSignature other = (MethodSignature) obj;
			if (desc == null) {
				if (other.desc != null)
					return false;
			} else if (!desc.equals(other.desc))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
}
