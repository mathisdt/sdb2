/*
 * This file is part of the Song Database (SDB).
 * 
 * SDB is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * SDB is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SDB. If not, see <http://www.gnu.org/licenses/>.
 */
package org.zephyrsoft.sdb2;

import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

/**
 * Configures the {@link Feature}s.
 * 
 * @author Mathis Dirksen-Thedens
 */
public class FeatureManagerProvider implements org.togglz.core.spi.FeatureManagerProvider {
	
	private static FeatureManager featureManager;
	
	@Override
	public int priority() {
		return 30;
	}
	
	@Override
	public synchronized FeatureManager getFeatureManager() {
		
		if (featureManager == null) {
			featureManager = new FeatureManagerBuilder()
				.featureEnum(Feature.class)
				.stateRepository(new InMemoryStateRepository())
				.userProvider(new NoOpUserProvider())
				.build();
			
			// enable experimental features only if "-Dexperimental=true" was set on start
			boolean enabled = System.getProperty("experimental") != null
				&& System.getProperty("experimental").equalsIgnoreCase("true");
			
			featureManager.setFeatureState(new FeatureState(Feature.HIGHLIGHT_FILTER_MATCHES, enabled));
		}
		
		return featureManager;
		
	}
	
}
