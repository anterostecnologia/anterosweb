package br.com.anteros.anteros.web.support.cloud;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.UUID;

import br.com.anteros.core.utils.StringUtils;
import br.com.anteros.nextcloud.api.AnterosNextCloudConnector;
import br.com.anteros.nextcloud.api.filesharing.Share;
import br.com.anteros.nextcloud.api.filesharing.SharePermissions;
import br.com.anteros.nextcloud.api.filesharing.ShareType;
import br.com.anteros.persistence.session.ExternalFileManager;
import br.com.anteros.persistence.session.ResultInfo;

public class NextCloudFileManager implements ExternalFileManager {

	AnterosNextCloudConnector connector;

	public NextCloudFileManager(String serverName, boolean useHttps, int port, String username, String password) {
		connector = new AnterosNextCloudConnector(serverName, useHttps, port, username, password);
	}

	@Override
	public ResultInfo saveFile(String folderName, String fileName, byte[] fileContent) throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			fileName = UUID.randomUUID().toString();
		}
		if (StringUtils.isEmpty(folderName)) {
			folderName = "";
		}

		
		if (connector.fileExists(fileName)) {
			throw new ExternalFileManagerException("Arquivo já existe " + folderName + File.separator + fileName);
		}
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(fileContent);
			connector.uploadFile(bais, folderName + File.separator + fileName);
			Share doShare = connector.doShare(folderName + File.separator + fileName, ShareType.PUBLIC_LINK, "", false,
					"", new SharePermissions(1));
			return ResultInfo.of(doShare.getUrl(), new Long(fileContent.length), fileName);
		} catch (Exception exception) {
			throw new ExternalFileManagerException(
					"O upload do arquivo falhou " + fileName + " => " + exception.getMessage());
		}
	}

	@Override
	public void removeFile(String fileName) throws Exception {
		if (!connector.fileExists(fileName)) {
			throw new ExternalFileManagerException("Arquivo não encontrado " + fileName);
		}
		try {
			connector.removeFile(fileName);
		} catch (Exception exception) {
			throw new ExternalFileManagerException(
					"Não foi possível remover o arquivo " + fileName + " => " + exception.getMessage());
		}

	}

}
