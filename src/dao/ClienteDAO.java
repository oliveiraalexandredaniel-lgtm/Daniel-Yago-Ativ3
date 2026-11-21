package dao;

import factory.ConnectionFactory;
import modelo.Cliente;
import util.Validacao;
import util.ValidadorCPF;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a dados e regras de negócio de Cliente.
 *
 * Regras: nome e CPF obrigatórios; CPF validado matematicamente e único (inclusive
 * ao alterar); status inicial ativo; ID nunca é alterado; data_ultima_alteracao é
 * atualizada a cada alteração; exclusão lógica (UPDATE status = FALSE).
 */
public class ClienteDAO {

    private static final String SELECT_BASE =
            "SELECT ID_Cliente, Nome, CPF, Data_Nascimento, Telefone, Endereco, Bairro, Cidade, "
            + "Estado, CEP, status, data_ultima_alteracao FROM cliente";

    private static final String CPF_SOMENTE_DIGITOS = "REPLACE(REPLACE(CPF, '.', ''), '-', '')";

    /** Cadastra um cliente (sempre ativo) e preenche o ID gerado no objeto. */
    public void inserir(Cliente c) throws DAOException {
        validar(c);
        verificarCpfDuplicado(c.getCpf(), 0);

        String sql = "INSERT INTO cliente (Nome, CPF, Data_Nascimento, Telefone, Endereco, Bairro, "
                + "Cidade, Estado, CEP, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherCampos(ps, c);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
            c.setStatus(true);
            c.setDataUltimaAlteracao(null);
        } catch (SQLException e) {
            throw new DAOException("Erro ao cadastrar o cliente: " + e.getMessage(), e);
        }
    }

    /** Altera os dados de um cliente ativo. O ID e o status não são alterados. */
    public void alterar(Cliente c) throws DAOException {
        Cliente atual = buscarPorId(c.getId());
        if (atual == null) {
            throw new RegraNegocioException("Cliente não encontrado (ID " + c.getId() + ").");
        }
        if (!atual.isStatus()) {
            throw new RegraNegocioException("Cliente inativo não pode ser alterado. Reative-o primeiro.");
        }
        validar(c);
        verificarCpfDuplicado(c.getCpf(), c.getId());

        LocalDateTime agora = LocalDateTime.now().withNano(0);
        String sql = "UPDATE cliente SET Nome = ?, CPF = ?, Data_Nascimento = ?, Telefone = ?, Endereco = ?, "
                + "Bairro = ?, Cidade = ?, Estado = ?, CEP = ?, data_ultima_alteracao = ? WHERE ID_Cliente = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencherCampos(ps, c);
            ps.setTimestamp(10, Timestamp.valueOf(agora));
            ps.setInt(11, c.getId());