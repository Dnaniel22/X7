/*
 * Funciones de interfaz del cliente.
 * La persistencia y autenticación ahora pertenecen a Spring Boot.
 */

document.addEventListener("DOMContentLoaded", async function () {
  // ---------- Catálogo (catalogo.html) ----------
  // Thymeleaf ya dibuja los productos. JavaScript solo se usa para filtrar.
  if (document.getElementById("grillaProductos")) {
    const tarjetas = Array.from(document.querySelectorAll(".producto-card"));
    const buscador = document.getElementById("buscadorProductos");
    let categoriaActiva = "";

    const aplicarFiltros = function () {
      const texto = buscador.value.trim().toLowerCase();

      tarjetas.forEach((tarjeta) => {
        const categoria = tarjeta.dataset.categoria || "";
        const contenido = (tarjeta.dataset.texto || "").toLowerCase();
        const coincideCategoria = !categoriaActiva || categoria === categoriaActiva;
        const coincideTexto = !texto || contenido.includes(texto);
        tarjeta.classList.toggle("d-none", !(coincideCategoria && coincideTexto));
      });
    };

    document.querySelectorAll("[data-categoria]").forEach((btn) => {
      btn.addEventListener("click", () => {
        categoriaActiva = btn.dataset.categoria;
        document.querySelectorAll("[data-categoria]").forEach((b) => {
          b.classList.remove("btn-terracotta");
          b.classList.add("btn-outline-burgundy");
        });
        btn.classList.remove("btn-outline-burgundy");
        btn.classList.add("btn-terracotta");
        aplicarFiltros();
      });
    });

    buscador.addEventListener("input", aplicarFiltros);
  }

  // ---------- Pedido (pedido.html) ----------
  if (document.getElementById("pedTipoProducto")) {
    const selectProducto = document.getElementById("pedTipoProducto");
    const elTamano = document.getElementById("pedTamano");
    const elCantidad = document.getElementById("pedCantidad");
    const elTotalEstimado = document.getElementById("pedTotalEstimado");
    const elModalAuth = document.getElementById("modalAuthPedido");
    const modalAuthPedido = elModalAuth ? new bootstrap.Modal(elModalAuth) : null;
    let productos = [];

    try {
      productos = await X7.getProductos();
      selectProducto.innerHTML =
        '<option value="" selected disabled>Seleccione...</option>' +
        productos
          .map(
            (p) =>
              `<option value="${X7.escapeHtml(p.nombre)}">${X7.escapeHtml(p.nombre)} — S/ ${Number(p.precio || 0).toFixed(2)}</option>`
          )
          .join("") +
        '<option value="Otro / Personalizado">Otro / Personalizado</option>';

      const productoUrl = new URLSearchParams(window.location.search).get("producto");
      if (productoUrl) {
        selectProducto.value = productoUrl;
      }
    } catch (error) {
      const msj = document.getElementById("pedidoMensaje");
      msj.classList.remove("d-none");
      msj.classList.add("alert-danger");
      msj.textContent = "No se pudo cargar el catálogo desde el backend.";
    }

    const sesion = await X7.getSesion();
    if (sesion && ["cliente", "client"].includes(String(sesion.rol || "").toLowerCase())) {
      const usuario = sesion.usuario || sesion;
      document.getElementById("pedNombre").value = usuario.nombre || "";
      document.getElementById("pedTelefono").value = usuario.telefono || "";
      document.getElementById("pedDireccion").value = usuario.direccion || "";
    }

    const calcularMonto = function () {
      const nombreProducto = selectProducto.value;
      const producto = productos.find((p) => p.nombre === nombreProducto);
      const factor = elTamano.selectedOptions[0]
        ? Number(elTamano.selectedOptions[0].dataset.factor)
        : null;
      const cantidad = Number(elCantidad.value) || 1;

      if (producto && factor) {
        return "S/ " + (Number(producto.precio) * factor * cantidad).toFixed(2);
      }
      return "Por confirmar";
    };

    const actualizarTotalEstimado = function () {
      elTotalEstimado.value = calcularMonto();
    };

    [selectProducto, elTamano, elCantidad].forEach((el) => {
      el.addEventListener("change", actualizarTotalEstimado);
      el.addEventListener("input", actualizarTotalEstimado);
    });
    actualizarTotalEstimado();

    document.getElementById("formPedido").addEventListener("submit", async function (e) {
      e.preventDefault();

      const sesionActual = await X7.getSesion();
      const rol = String(sesionActual?.rol || "").toLowerCase();
      if (!sesionActual || !["cliente", "client"].includes(rol)) {
        if (modalAuthPedido) modalAuthPedido.show();
        return;
      }

      const pedido = {
        tipo: "Pedido",
        producto: selectProducto.value,
        nombreContacto: document.getElementById("pedNombre").value.trim(),
        telefono: document.getElementById("pedTelefono").value.trim(),
        direccion: document.getElementById("pedDireccion").value.trim(),
        detalle: [
          elTamano.value || null,
          Number(elCantidad.value) > 1 ? "Cantidad: " + elCantidad.value : null,
          document.getElementById("pedColores").value || null,
          document.getElementById("pedDetalles").value || null,
        ]
          .filter(Boolean)
          .join(" · "),
        fechaEntrega: document.getElementById("pedFecha").value,
        monto: calcularMonto(),
      };

      const msj = document.getElementById("pedidoMensaje");
      try {
        await X7.guardarPedido(pedido);
        msj.classList.remove("d-none", "alert-danger");
        msj.classList.add("alert-success");
        msj.innerHTML = '<i class="bi bi-check-circle me-2"></i>¡Pedido registrado! Redirigiendo a tu panel...';
        setTimeout(() => {
          window.location.href = "/cliente/panel";
        }, 900);
      } catch (error) {
        msj.classList.remove("d-none", "alert-success");
        msj.classList.add("alert-danger");
        msj.textContent = error.message;
      }
    });
  }

  // ---------- Reservar (reservar.html) ----------
  if (document.getElementById("formReserva")) {
    const elModalAuthReserva = document.getElementById("modalAuthReserva");
    const modalAuthReserva = elModalAuthReserva
      ? new bootstrap.Modal(elModalAuthReserva)
      : null;

    document.getElementById("formReserva").addEventListener("submit", async function (e) {
      e.preventDefault();

      const sesionActual = await X7.getSesion();
      const rol = String(sesionActual?.rol || "").toLowerCase();
      if (!sesionActual || !["cliente", "client"].includes(rol)) {
        if (modalAuthReserva) modalAuthReserva.show();
        return;
      }

      const pedido = {
        tipo: "Catering",
        producto: document.getElementById("resTipoEvento").value,
        detalle: [
          document.getElementById("resInvitados").value
            ? document.getElementById("resInvitados").value + " invitados"
            : null,
          document.getElementById("resLugar").value || null,
          document.getElementById("resRequerimientos").value || null,
        ]
          .filter(Boolean)
          .join(" · "),
        fechaEntrega: document.getElementById("resFecha").value,
        monto: "Por confirmar",
      };

      const msj = document.getElementById("reservaMensaje");
      try {
        await X7.guardarPedido(pedido);
        msj.classList.remove("d-none", "alert-danger");
        msj.classList.add("alert-success");
        msj.innerHTML = '<i class="bi bi-check-circle me-2"></i>¡Solicitud registrada! Redirigiendo a tu panel...';
        setTimeout(() => {
          window.location.href = "/cliente/panel";
        }, 900);
      } catch (error) {
        msj.classList.remove("d-none", "alert-success");
        msj.classList.add("alert-danger");
        msj.textContent = error.message;
      }
    });
  }

  // ---------- Login (autenticacion/login.html) ----------
  if (document.getElementById("loginForm")) {
    document.getElementById("loginForm").addEventListener("submit", async function (e) {
      e.preventDefault();

      const correo = document.getElementById("email").value.trim();
      const clave = document.getElementById("password").value;
      const mensaje = document.getElementById("loginMensaje");
      mensaje.classList.add("d-none");

      try {
        const sesion = await X7.iniciarSesion(correo, clave);
        const rol = String(sesion?.rol || "").toLowerCase();
        window.location.href =
          rol === "administrador" || rol === "admin"
            ? "/admin/panel"
            : "/cliente/panel";
      } catch (error) {
        mensaje.classList.remove("d-none");
        mensaje.innerHTML =
          '<i class="bi bi-exclamation-triangle me-2"></i>' +
          X7.escapeHtml(error.message);
      }
    });
  }

  // ---------- Registro (autenticacion/registro.html) ----------
  if (document.getElementById("formRegistro")) {
    document.getElementById("formRegistro").addEventListener("submit", async function (e) {
      e.preventDefault();

      const mensaje = document.getElementById("registroMensaje");
      mensaje.classList.add("d-none");

      const datos = {
        nombre: document.getElementById("regNombre").value.trim(),
        email: document.getElementById("regEmail").value.trim(),
        telefono: document.getElementById("regTelefono").value.trim(),
        direccion: document.getElementById("regDireccion").value.trim(),
        password: document.getElementById("regPassword").value,
      };
      const password2 = document.getElementById("regPassword2").value;

      if (datos.password !== password2) {
        mensaje.classList.remove("d-none");
        mensaje.innerHTML = '<i class="bi bi-exclamation-triangle me-2"></i>Las contraseñas no coinciden.';
        return;
      }

      try {
        await X7.registrarUsuario(datos);
        await X7.iniciarSesion(datos.email, datos.password);
        window.location.href = "/cliente/panel";
      } catch (error) {
        mensaje.classList.remove("d-none");
        mensaje.innerHTML =
          '<i class="bi bi-exclamation-triangle me-2"></i>' +
          X7.escapeHtml(error.message);
      }
    });
  }

  // ---------- Panel del cliente (panel/panel-cliente.html) ----------
  if (document.getElementById("tablaPedidos")) {
    const sesion = await X7.getSesion();
    const rol = String(sesion?.rol || "").toLowerCase();

    if (!sesion || !["cliente", "client"].includes(rol)) {
      window.location.href = "/login";
      return;
    }

    const usuario = sesion.usuario || sesion;
    if (usuario) {
      const primerNombre = String(usuario.nombre || "Cliente").split(" ")[0];
      document.getElementById("saludoNombre").textContent = "Hola, " + primerNombre + " 👋";
      document.getElementById("datNombre").textContent = usuario.nombre || "—";
      document.getElementById("datCorreo").textContent = usuario.email || "—";
      document.getElementById("datTelefono").textContent = usuario.telefono || "—";
      document.getElementById("datDireccion").textContent = usuario.direccion || "—";
    }

    const tabla = document.getElementById("tablaPedidos");
    try {
      const pedidos = await X7.pedidosDeCliente();

      if (!pedidos || pedidos.length === 0) {
        tabla.innerHTML = `
          <tr>
            <td colspan="5" class="text-center text-muted py-4">
              Aún no tienes pedidos registrados. <a href="/pedido">Encarga tu primer pedido aquí</a>.
            </td>
          </tr>`;
      } else {
        const estadoBadge = {
          PENDIENTE: '<span class="badge bg-warning text-dark">PENDIENTE</span>',
          "EN PREPARACIÓN": '<span class="badge bg-info text-dark">EN PREPARACIÓN</span>',
          ENTREGADO: '<span class="badge bg-success">ENTREGADO</span>',
        };

        tabla.innerHTML = pedidos
          .map(
            (p) => `
              <tr>
                <td class="fw-bold">${X7.escapeHtml(p.codigo)}</td>
                <td>${X7.escapeHtml(p.tipo)}: ${X7.escapeHtml(p.producto)}${p.detalle ? " — " + X7.escapeHtml(p.detalle) : ""}</td>
                <td>${X7.escapeHtml(p.fechaEntrega || "-")}</td>
                <td class="fw-bold">${X7.escapeHtml(p.monto)}</td>
                <td>${estadoBadge[p.estado] || X7.escapeHtml(p.estado)}</td>
              </tr>`
          )
          .join("");
      }
    } catch (error) {
      tabla.innerHTML = `
        <tr><td colspan="5" class="text-center text-danger py-4">${X7.escapeHtml(error.message)}</td></tr>`;
    }
  }
});
