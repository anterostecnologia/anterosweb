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
	private String defaultFolder;

	public NextCloudFileManager(String serverName, boolean useHttps, int port, String username, String password,
			String defaultFolder) {
		connector = new AnterosNextCloudConnector(serverName, useHttps, port, username, password);
		this.defaultFolder = defaultFolder;
	}

	@Override
	public ResultInfo saveFile(String folderName, String fileName, byte[] fileContent) throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			fileName = UUID.randomUUID().toString();
		}
		if (StringUtils.isEmpty(folderName)) {
			folderName = defaultFolder;
		}

		String[] folderNameSplit = folderName.split("\\/");
		String fldTemp = "";
		boolean appendDelimiter = false;
		for (String fld : folderNameSplit) {
			if (appendDelimiter) {
				fldTemp += "/";
			}
			fldTemp += fld;
			if (!connector.folderExists(fldTemp)) {
				connector.createFolder(fldTemp);
			}
			appendDelimiter = true;
		}

		if (connector.fileExists(folderName + File.separator + fileName)) {
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
	public void removeFile(String folderName, String fileName) throws Exception {
		if (StringUtils.isEmpty(folderName)) {
			folderName = defaultFolder;
		}

		if (!connector.fileExists(folderName + File.separator + fileName)) {
			throw new ExternalFileManagerException("Arquivo não encontrado " + folderName + File.separator + fileName);
		}
		try {
			connector.removeFile(folderName + File.separator + fileName);
		} catch (Exception exception) {
			throw new ExternalFileManagerException("Não foi possível remover o arquivo " + folderName + File.separator
					+ fileName + " => " + exception.getMessage());
		}

	}

}
