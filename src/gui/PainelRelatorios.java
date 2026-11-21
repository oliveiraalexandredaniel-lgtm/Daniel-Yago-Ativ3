package gui;

import dao.AnimalDAO;
import dao.ClienteDAO;
import dao.DAOException;
import modelo.Animal;
import modelo.Cliente;
import util.GeradorRelatorioTXT;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Tela de relatórios: gera os arquivos .txt e informa onde foram salvos. */
public class PainelRelatorios extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter HORA_LOG = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();

    private final JTextField txtPasta = Tema.campo();
    private final JComboBox<String> cmbMesAnimais = new JComboBox<>(opcoesDeMes());
    private final JComboBox<String> cmbMesClientes = new JComboBox<>(opcoesDeMes());
    private final JTextArea txtLog = new JTextArea(6, 40);

    public PainelRelatorios() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Tema.FUNDO);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        txtPasta.setText(Paths.get("relatorios").toAbsolutePath().toString());
        int mesAtual = LocalDate.now().getMonthValue() - 1;
        cmbMesAnimais.setSelectedIndex(mesAtual);
        cmbMesClientes.setSelectedIndex(mesAtual);
        Tema.estilizarCampo(cmbMesAnimais);
        Tema.estilizarCampo(cmbMesClientes);

        JPanel topo = Tema.fundo();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        JLabel titulo = Tema.titulo("Relatórios");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = Tema.subtitulo("Gere arquivos .txt (UTF-8) com os dados do banco. Os relatórios incluem apenas registros ativos.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        topo.add(titulo);
        topo.add(Box.createVerticalStrut(4));
        topo.add(sub);
        add(topo, BorderLayout.NORTH);

        JPanel centro = Tema.fundo();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.add(criarCartaoPasta());
        centro.add(Box.createVerticalStrut(14));
        centro.add(criarCartaoRelatorio("1. Todos os clientes e seus animais",
                "Lista cada cliente com CPF e seus animais (nome, raça e data de nascimento).",
                null, this::gerarGeral));
        centro.add(Box.createVerticalStrut(14));
        centro.add(criarCartaoRelatorio("2. Animais aniversariantes do mês",
                "Lista os animais ativos que fazem aniversário no mês selecionado, com os dados do tutor.",
                cmbMesAnimais, this::gerarAniversariantesAnimais));
        centro.add(Box.createVerticalStrut(14));
        centro.add(criarCartaoRelatorio("3. Clientes aniversariantes do mês",
                "Lista os clientes ativos que fazem aniversário no mês selecionado.",
                cmbMesClientes, this::gerarAniversariantesClientes));

        JScrollPane scrollCentro = new JScrollPane(centro);
        scrollCentro.setBorder(null);
        scrollCentro.getViewport().setBackground(Tema.FUNDO);
        scrollCentro.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollCentro, BorderLayout.CENTER);

        add(criarCartaoLog(), BorderLayout.SOUTH);
    }

    @Override
    public void atualizar() {
        // Não há tabela para recarregar; os dados são consultados sob demanda ao gerar cada relatório.
    }

    private JPanel criarCartaoPasta() {
        JPanel cartao = Tema.cartao();
        cartao.setLayout(new BorderLayout(12, 8));
        cartao.add(Tema.secao("Pasta de destino dos relatórios"), BorderLayout.NORTH);

        JPanel linha = new JPanel(new GridBagLayout());
        linha.setOpaque(false);

        GridBagConstraints cCampo = new GridBagConstraints();
        cCampo.gridx = 0;
        cCampo.gridy = 0;
        cCampo.weightx = 1.0;
        cCampo.fill = GridBagConstraints.HORIZONTAL;
        cCampo.insets = new Insets(0, 0, 0, 8);
        linha.add(txtPasta, cCampo);

        JButton btnEscolher = Tema.botaoSecundario("Escolher pasta...");
        btnEscolher.addActionListener(e -> escolherPasta());

        JButton btnAbrir = Tema.botaoSecundario("Abrir pasta");
        btnAbrir.addActionListener(e -> abrirPasta());

        JPanel botoesPasta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoesPasta.setOpaque(false);
        botoesPasta.add(btnEscolher);
        botoesPasta.add(btnAbrir);

        GridBagConstraints cBotoes = new GridBagConstraints();
        cBotoes.gridx = 1;
        cBotoes.gridy = 0;
        linha.add(botoesPasta, cBotoes);

        cartao.add(linha, BorderLayout.CENTER);
        return cartao;
    }

    private JPanel criarCartaoRelatorio(String titulo, String descricao, JComboBox<String> filtroMes, Runnable acao) {
        JPanel cartao = Tema.cartao();
        cartao.setLayout(new BorderLayout(16, 0));

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        JLabel lblTitulo = Tema.secao(titulo);
        lblTitulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblDesc = Tema.subtitulo(descricao);
        lblDesc.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblDesc);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        direita.setOpaque(false);
        if (filtroMes != null) {
            direita.add(Tema.rotulo("Mês:"));
            direita.add(filtroMes);
        }
        JButton btnGerar = Tema.botaoPrimario("Gerar .txt");
        btnGerar.addActionListener(e -> acao.run());
        direita.add(btnGerar);

        cartao.add(textos, BorderLayout.CENTER);
        cartao.add(direita, BorderLayout.EAST);
        return cartao;
    }

    private JPanel criarCartaoLog() {
        JPanel cartao = Tema.cartao();
        cartao.setLayout(new BorderLayout(0, 8));
        cartao.add(Tema.secao("Histórico de geração"), BorderLayout.NORTH);

        txtLog.setEditable(false);
        txtLog.setFont(Tema.FONTE);
        txtLog.setForeground(Tema.TEXTO);
        txtLog.setBackground(Tema.FUNDO);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        txtLog.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(txtLog);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDA, 1));
        cartao.add(scroll, BorderLayout.CENTER);
        return cartao;
    }

    private Path prepararPasta() throws IOException {
        String caminho = txtPasta.getText().trim();
        if (caminho.isEmpty()) {
            caminho = "relatorios";
            txtPasta.setText(Paths.get(caminho).toAbsolutePath().toString());
        }
        Path pasta = Paths.get(caminho);
        Files.createDirectories(pasta);
        return pasta;
    }

    private void escolherPasta() {
        JFileChooser fc = new JFileChooser(txtPasta.getText().trim());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Selecione a pasta para salvar os relatórios");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selecionada = fc.getSelectedFile();
            txtPasta.setText(selecionada.getAbsolutePath());
        }
    }

    private void abrirPasta() {
        try {
            Path pasta = prepararPasta();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pasta.toFile());
            } else {
                Mensagens.aviso(this, "Abertura automática não suportada neste sistema. Pasta: " + pasta.toAbsolutePath());
            }
        } catch (IOException e) {
            Mensagens.erro(this, "Não foi possível abrir a pasta: " + e.getMessage());
        }
    }

    private void gerarGeral() {
        try {
            Path pasta = prepararPasta();
            List<Cliente> clientes = clienteDAO.pesquisar("", false);
            List<Animal> animais = animalDAO.pesquisar("", false);

            Map<Cliente, List<Animal>> mapa = new LinkedHashMap<>();
            for (Cliente c : clientes) {
                List<Animal> petsDoCliente = animais.stream()
                        .filter(a -> a.getIdCliente() == c.getId())
                        .toList();
                mapa.put(c, petsDoCliente);
            }

            Path arquivo = GeradorRelatorioTXT.gerarClientesEAnimais(pasta, mapa);
            registrarSucesso("Relatório geral gerado em: " + arquivo.toAbsolutePath());
        } catch (DAOException | IOException e) {
            Mensagens.erro(this, "Erro ao gerar relatório geral: " + e.getMessage());
        }
    }

    private void gerarAniversariantesAnimais() {
        try {
            Path pasta = prepararPasta();
            int mes = cmbMesAnimais.getSelectedIndex() + 1;
            List<Animal> aniversariantes = animalDAO.pesquisar("", false).stream()
                    .filter(a -> a.getDataNascimento() != null && a.getDataNascimento().getMonthValue() == mes)
                    .toList();

