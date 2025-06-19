package mybank.controller;

import com.mybank.controller.AgencyDocumentController;
import com.mybank.controller.FolderController;
import com.mybank.controller.UserController;
import com.mybank.model.Agency;
import com.mybank.model.AgencyDocument;
import com.mybank.model.Alert;
import com.mybank.model.Folder;
import com.mybank.model.User;
import com.mybank.service.AgencyDocumentService;
import com.mybank.service.AlertService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.StreamedContent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgencyDocumentControllerTest {

    @InjectMocks
    private AgencyDocumentController controller;

    @Mock
    private AgencyDocumentService agencyDocumentService;

    @Mock
    private FolderController folderController;

    @Mock
    private AlertService alertService;

    @Mock
    private UserController userController;

    @Mock
    private UploadedFile uploadedFile;

    private Folder folder;
    private User director;
    private User creator;

    @BeforeEach
    void setUp() {
        folder = new Folder();
        creator = new User();
        director = new User();
        Agency agency = new Agency();

        creator.setUsername("creator");
        director.setUsername("director");
        agency.setDirector(director);
        creator.setAgency(agency);
        folder.setCreatedBy(creator);
    }

    @Test
    void testUploadSuccess_byDirector() {
        when(folderController.getSelectedFolder()).thenReturn(folder);
        when(uploadedFile.getContent()).thenReturn("data".getBytes());
        when(userController.getCurrentUser()).thenReturn(director);

        controller.setUploadedFile(uploadedFile);
        controller.setCustomFileName("TestDoc");

        controller.upload();

        verify(agencyDocumentService).save(any(AgencyDocument.class));
        verify(alertService).createAlert(any(Alert.class));
        assertEquals("✅ Fichier ajouté avec succès.", controller.getUploadMessage());
    }

    @Test
    void testUploadFailure_missingFields() {
        controller.setUploadedFile(null);
        controller.setCustomFileName(" ");
        when(folderController.getSelectedFolder()).thenReturn(null);

        controller.upload();

        assertEquals("❌ Veuillez saisir un nom de fichier et sélectionner un fichier.", controller.getUploadMessage());
    }

    @Test
    void testDownload() {
        AgencyDocument doc = new AgencyDocument();
        doc.setFileName("example.pdf");
        doc.setFileData("sample-data".getBytes());

        StreamedContent content = controller.download(doc);

        assertEquals("example.pdf", content.getName());
        assertNotNull(content.getStream());
    }

    @Test
    void testGetDocumentsForSelectedFolder_whenFolderExists() {
        when(folderController.getSelectedFolder()).thenReturn(folder);
        when(agencyDocumentService.findByFolder(folder)).thenReturn(List.of(new AgencyDocument()));

        List<AgencyDocument> result = controller.getDocumentsForSelectedFolder();
        assertEquals(1, result.size());
    }

    @Test
    void testGetDocumentsForSelectedFolder_whenNoFolder() {
        when(folderController.getSelectedFolder()).thenReturn(null);
        List<AgencyDocument> result = controller.getDocumentsForSelectedFolder();
        assertTrue(result.isEmpty());
    }
}

