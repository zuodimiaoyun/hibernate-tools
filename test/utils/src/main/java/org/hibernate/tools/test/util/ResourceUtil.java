/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 * 
 * Copyright 2017-2020 Red Hat, Inc.
 *
 * Licensed under the GNU Lesser General Public License (LGPL), 
 * version 2.1 or later (the "License").
 * You may not use this file except in compliance with the License.
 * You may read the licence in the 'lgpl.txt' file in the root folder of 
 * project or obtain a copy at
 *
 *     http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.tools.test.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ResourceUtil {

	public static String getResourcesLocation(Object test) {
		return '/' + test.getClass().getPackage().getName().replace('.', '/') + '/';
	}
	
	public static void createResources(Object test, String[] resources, File resourcesDir) {
		try {
			String defaultResourceLocation = getResourcesLocation(test);
			for (String resource : resources) {
				String resourceLocation = 
						(resource.startsWith("/")) 
						? resource : defaultResourceLocation + resource;
				InputStream inputStream = test
						.getClass()
						.getResourceAsStream(resourceLocation); 
				File resourceFile = new File(resourcesDir, resource);
				File parent = resourceFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				Files.copy(inputStream, resourceFile.toPath());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
