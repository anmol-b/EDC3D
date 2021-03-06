
package edu.dhbw.andobjviewer.graphics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLUtils;

import edu.dhbw.andar.ARObject;
import edu.dhbw.andobjviewer.models.Group;
import edu.dhbw.andobjviewer.models.Material;
import edu.dhbw.andobjviewer.models.Model;


/**
 * represents a 3d model.
 * Edu-Construct 3D
 *
 */
public class Model3D extends ARObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Model model;
	private Group[] texturedGroups;
	private Group[] nonTexturedGroups;
	private HashMap<Material, Integer> textureIDs = new HashMap<Material, Integer>();
	
	public Model3D(Model model,String pattern) {
		super("model", pattern, 80.0, new double[]{0,0});
		this.model = model;
		model.finalize();
		//separate texture from non textured groups for performance reasons
		Vector<Group> groups = model.getGroups();
		Vector<Group> texturedGroups = new Vector<Group>();
		Vector<Group> nonTexturedGroups = new Vector<Group>();
		for (Iterator<Group> iterator = groups.iterator(); iterator.hasNext();) {
			Group currGroup = iterator.next();
			if(currGroup.isTextured()) {
				texturedGroups.add(currGroup);
			} else {
				nonTexturedGroups.add(currGroup);
			}			
		}
		this.texturedGroups = texturedGroups.toArray(new Group[texturedGroups.size()]);
		this.nonTexturedGroups = nonTexturedGroups.toArray(new Group[nonTexturedGroups.size()]);	
	}
	
	@Override
	public void init(GL10 gl){
		int[]  tmpTextureID = new int[1];
		//load textures of every material(that has a texture):
		Iterator<Material> materialI = model.getMaterials().values().iterator();
		while (materialI.hasNext()) {
			Material material = (Material) materialI.next();
			if(material.hasTexture()) {
				//load texture
				gl.glGenTextures(1, tmpTextureID, 0);
				gl.glBindTexture(GL10.GL_TEXTURE_2D, tmpTextureID[0]);
				textureIDs.put(material, tmpTextureID[0]);
				GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, material.getTexture(),0);
				material.getTexture().recycle();
				gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
				gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); 
			}
		}
		
		//transfer vertices to video memory
	}
	
	
	@Override
	public void draw(GL10 gl) {
		super.draw(gl);
		
		//gl = (GL10) GLDebugHelper.wrap(gl, GLDebugHelper.CONFIG_CHECK_GL_ERROR, log);
		//do positioning:
		gl.glScalef(model.scale, model.scale, model.scale);
		gl.glTranslatef(model.xpos, model.ypos, model.zpos);
		gl.glRotatef(model.xrot, 1, 0, 0);
		gl.glRotatef(model.yrot, 0, 1, 0);
		gl.glRotatef(model.zrot, 0, 0, 1);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		
		//first draw non textured groups
		gl.glDisable(GL10.GL_TEXTURE_2D);
		int cnt = nonTexturedGroups.length;
		for (int i = 0; i < cnt; i++) {
			Group group = nonTexturedGroups[i];
			Material mat = group.getMaterial();
			if(mat != null) {
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, mat.specularlight);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat.ambientlight);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat.diffuselight);
				gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat.shininess);
			}
			gl.glVertexPointer(3,GL10.GL_FLOAT, 0, group.vertices);
	        gl.glNormalPointer(GL10.GL_FLOAT,0, group.normals);	        
	        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, group.vertexCount);
		}
		
		//now we can continue with textured ones
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		cnt = texturedGroups.length;
		for (int i = 0; i < cnt; i++) {
			Group group = texturedGroups[i];
			Material mat = group.getMaterial();
			if(mat != null) {
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, mat.specularlight);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mat.ambientlight);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mat.diffuselight);
				gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat.shininess);
				if(mat.hasTexture()) {
					gl.glTexCoordPointer(2,GL10.GL_FLOAT, 0, group.texcoords);
					gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIDs.get(mat).intValue());
				}
			}
			gl.glVertexPointer(3,GL10.GL_FLOAT, 0, group.vertices);
	        gl.glNormalPointer(GL10.GL_FLOAT,0, group.normals);	        
	        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, group.vertexCount);
		}
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	/**
	 * 
	 *Edu-Construct 3D
	 *
	 */
	
}
