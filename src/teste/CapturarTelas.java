package teste;

import gui.JanelaPrincipal;
import principal.Main;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ferramenta usada para gerar as capturas de tela do manual do usuário e para exercitar
 * a interface de ponta a ponta: abre a janela real, navega pelas telas, preenche formulários,
 * clica nos botões (inclusive os que abrem diálogos) e salva PNGs.
 *
 * Uso (precisa de ambiente gráfico; em Linux sem monitor use xvfb-run):
 *   java -cp "bin:lib/*" teste.CapturarTelas pasta_de_saida
 *
 * ATENÇÃO: cadastra registros reais no banco e gera relatórios na pasta relatorios/.
 * Execute o Script.sql depois para voltar ao estado inicial.
 */
public class CapturarTelas {

    private static Robot robot;
    private static JanelaPrincipal janela;
    private static File saida;

    public static void main(String[] args) throws Exception {
        saida = new File(args.length > 0 ? args[0] : "docs/telas");
        saida.mkdirs();
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        Main.configurarAparencia();
        robot = new Robot();

        SwingUtilities.invokeAndWait(() -> {
            janela = new JanelaPrincipal();
            janela.setLocation(20, 20);
            janela.setVisible(true);
        });
        esperar(1500);

        // 1. Painel inicial
        capturarJanela("01_painel_inicial");

        // 2. Clientes: listagem
        navegar("Clientes");
        capturarJanela("02_clientes_listagem");

        // 3. Clientes: formulário preenchido e cadastro com sucesso
        List<JTextField> campos = campos();
        preencher(campos.get(1), "Marina Costa");
        preencher(campos.get(2), "168.995.350-09");
        preencher(campos.get(3), "08/09/1992");
        preencher(campos.get(4), "(12) 99555-4321");
        preencher(campos.get(6), "Rua das Acácias, 77");
        preencher(campos.get(7), "Jardim das Colinas");
        preencher(campos.get(8), "São José dos Campos");
        preencher(campos.get(9), "12242-010");
        selecionarNoCombo("SP");
        capturarJanela("03_clientes_formulario_preenchido");
        clicarEcapturarDialogo("Cadastrar", "04_clientes_cadastro_sucesso");
        capturarJanela("05_clientes_apos_cadastro");

        // 4. Clientes: CPF inválido
        campos = campos();
        preencher(campos.get(1), "Cliente Teste");
        preencher(campos.get(2), "123.456.789-00");
        clicarEcapturarDialogo("Cadastrar", "06_clientes_cpf_invalido");

        // 5. Clientes: CPF duplicado
        preencher(campos.get(2), "123.456.789-09");
        clicarEcapturarDialogo("Cadastrar", "07_clientes_cpf_duplicado");
