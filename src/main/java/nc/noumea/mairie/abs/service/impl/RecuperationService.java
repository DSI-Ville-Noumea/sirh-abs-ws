package nc.noumea.mairie.abs.service.impl;

import java.util.Date;

import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.service.IRecuperationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecuperationService implements IRecuperationService {

	@Autowired
	private IRecuperationRepository recuperationRepository;
	
	@Override
	public int addRecuperationToAgent(Integer idAgent, Date dateMonday, Integer minutes) {
		// TODO Auto-generated method stub
		return 0;
	}	
	
}
