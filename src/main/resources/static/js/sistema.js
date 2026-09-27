/*
 * Funciones de interfaz del panel administrador.
 * Los datos ahora se consultan y modifican mediante Spring Boot.
 */

document.addEventListener("DOMContentLoaded", async function () {
  if (!document.getElementById("tablaPedidosAdmin")) return;

  const sesion = await X7.getSesion();
  const rol = String(sesion?.rol || "").toLowerCase();
  if (!sesion || !["administrador", "admin"].includes(rol)) {
    window.location.href = "/login";
    return;
  }

  const btnSalir = document.getElementById("btnSalirAdmin");
  if (btnSalir) {
    btnSalir.addEventListener("click", async function (e) {
      e.preventDefault();
      try {
        await X7.cerrarSesion();
      } finally {
        window.location.href = "/";
      }
    });
  }

  const SIGUIENTE_ESTADO = {
    PENDIENTE: "EN PREPARACIÓN",
    "EN PREPARACIÓN": "ENTREGADO",
    ENTREGADO: "ENTREGADO",
  };

  const ESTADO_BADGE = {
    PENDIENTE: '<span class="badge bg-warning text-dark">PENDIENTE</span>',
    "EN PREPARACIÓN": '<span class="badge bg-info text-dark">EN PREPARACIÓN</span>',
    ENTREGADO: '<span class="badge bg-success">ENTREGADO</span>',
  };

  const renderPedidosAdmin = async function () {
    const tabla = document.getElementById("tablaPedidosAdmin");
    tabla.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Cargando pedidos...</td></tr>';

    try {
      const pedidos = await X7.getPedidos();

      if (!pedidos || pedidos.length === 0) {
        tabla.innerHTML = `
          <tr>
            <td colspan="7" class="text-center text-muted py-4">Todavía no hay pedidos ni cotizaciones registradas.</td>
          </tr>`;
        return;
      }

      tabla.innerHTML = pedidos
        .map((p) => {
          const cliente = p.cliente?.nombre || p.nombreCliente || p.email || "Cliente";
          return `
            <tr>
              <td class="fw-bold">${X7.escapeHtml(p.codigo)}</td>
              <td>${X7.escapeHtml(cliente)}</td>
              <td>${X7.escapeHtml(p.tipo)}: ${X7.escapeHtml(p.producto)}${p.detalle ? " — " + X7.escapeHtml(p.detalle) : ""}</td>
              <td>${X7.escapeHtml(p.fechaEntrega || "-")}</td>
              <td class="fw-bold">${X7.escapeHtml(p.monto)}</td>
              <td>${ESTADO_BADGE[p.estado] || X7.escapeHtml(p.estado)}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-success" title="Avanzar estado" data-avanzar="${X7.escapeHtml(p.id)}" data-estado="${X7.escapeHtml(p.estado)}" ${p.estado === "ENTREGADO" ? "disabled" : ""}>
                  <i class="bi bi-check-lg"></i>
                </button>
                <button class="btn btn-sm btn-danger" title="Eliminar" data-eliminar="${X7.escapeHtml(p.id)}">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>`;
        })
        .join("");

      tabla.querySelectorAll("[data-avanzar]").forEach((btn) => {
        btn.addEventListener("click", async () => {
          const siguiente = SIGUIENTE_ESTADO[btn.dataset.estado];
          if (!siguiente) return;
          btn.disabled = true;
          try {
            await X7.actualizarEstadoPedido(btn.dataset.avanzar, siguiente);
            await renderPedidosAdmin();
          } catch (error) {
            alert(error.message);
            btn.disabled = false;
          }
        });
      });

      tabla.querySelectorAll("[data-eliminar]").forEach((btn) => {
        btn.addEventListener("click", async () => {
          if (!confirm("¿Seguro que deseas eliminar este pedido?")) return;
          try {
            await X7.eliminarPedido(btn.dataset.eliminar);
            await renderPedidosAdmin();
          } catch (error) {
            alert(error.message);
          }
        });
      });
    } catch (error) {
      tabla.innerHTML = `
        <tr><td colspan="7" class="text-center text-danger py-4">${X7.escapeHtml(error.message)}</td></tr>`;
    }
  };

  const renderProductosAdmin = async function () {
    const tabla = document.getElementById("tablaProductosAdmin");
    tabla.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">Cargando productos...</td></tr>';

    try {
      const productos = await X7.getProductos();

      if (!productos || productos.length === 0) {
        tabla.innerHTML = `
          <tr>
            <td colspan="5" class="text-center text-muted py-4">Todavía no hay productos en el catálogo.</td>
          </tr>`;
        return;
      }

      tabla.innerHTML = productos
        .map(
          (p) => `
            <tr>
              <td><img src="${X7.escapeHtml(p.imagen || "")}" alt="${X7.escapeHtml(p.nombre)}" class="rounded-3" style="width:50px;height:50px;object-fit:cover;"></td>
              <td class="fw-bold">${X7.escapeHtml(p.nombre)}</td>
              <td><span class="badge badge-gold">${X7.escapeHtml(p.categoria)}</span></td>
              <td class="fw-bold">S/ ${Number(p.precio || 0).toFixed(2)}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-danger" title="Eliminar" data-eliminar-producto="${X7.escapeHtml(p.id)}">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>`
        )
        .join("");

      tabla.querySelectorAll("[data-eliminar-producto]").forEach((btn) => {
        btn.addEventListener("click", async () => {
          if (!confirm("¿Seguro que deseas eliminar este producto?")) return;
          try {
            await X7.eliminarProducto(btn.dataset.eliminarProducto);
            await renderProductosAdmin();
          } catch (error) {
            alert(error.message);
          }
        });
      });
    } catch (error) {
      tabla.innerHTML = `
        <tr><td colspan="5" class="text-center text-danger py-4">${X7.escapeHtml(error.message)}</td></tr>`;
    }
  };

  const formProducto = document.getElementById("formProducto");
  if (formProducto) {
    formProducto.addEventListener("submit", async function (e) {
      e.preventDefault();
      const mensaje = document.getElementById("productoMensaje");
      mensaje.classList.add("d-none");

      const producto = {
        nombre: document.getElementById("prodNombre").value.trim(),
        categoria: document.getElementById("prodCategoria").value,
        etiqueta: document.getElementById("prodEtiqueta").value.trim(),
        precio: Number(document.getElementById("prodPrecio").value),
        descripcion: document.getElementById("prodDescripcion").value.trim(),
        imagen: document.getElementById("prodImagen").value.trim(),
      };

      if (!producto.nombre || !producto.categoria || !producto.precio || !producto.descripcion) {
        mensaje.classList.remove("d-none");
        mensaje.innerHTML = '<i class="bi bi-exclamation-triangle me-2"></i>Completa todos los campos obligatorios (*).';
        return;
      }

      try {
        await X7.agregarProducto(producto);
        this.reset();
        bootstrap.Modal.getOrCreateInstance(document.getElementById("modalProducto")).hide();
        await renderProductosAdmin();
      } catch (error) {
        mensaje.classList.remove("d-none");
        mensaje.innerHTML =
          '<i class="bi bi-exclamation-triangle me-2"></i>' +
          X7.escapeHtml(error.message);
      }
    });
  }

  await Promise.all([renderPedidosAdmin(), renderProductosAdmin()]);
});
