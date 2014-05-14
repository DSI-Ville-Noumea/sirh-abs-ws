package nc.noumea.mairie.abs.service.rules.impl;

import org.junit.Test;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

@MockStaticEntityMethods
public class AbsAsaA50DataConsistencyRulesImplTest extends AbsAsaDataConsistencyRulesImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {

		super.impl = new AbsAsaA50DataConsistencyRulesImpl();
		super.allTest();
	}
}
